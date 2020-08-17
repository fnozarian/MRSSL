package com.aut.ceit.clustering.mrssl

import scala.collection.mutable.HashMap

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkContext.IntAccumulatorParam
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.Accumulator
import org.apache.spark.rdd.ShuffledRDD
import org.apache.spark.Logging

import com.aut.ceit.clustering.mrssl.util.IOHelper
import com.aut.ceit.clustering.mrssl.spatial.Point
import com.aut.ceit.clustering.mrssl.spatial.rdd.PartitioningSettings
import com.aut.ceit.clustering.mrssl.spatial.rdd.PointsPartitionedByBoxesRDD
import com.aut.ceit.clustering.mrssl.util.DebugHelper
import com.aut.ceit.clustering.mrssl.spatial.rdd.DataSpherePartitioner
import com.aut.ceit.clustering.ssl.HAC
import com.aut.ceit.clustering.ssl.DataSphere
import com.aut.ceit.clustering.mrssl.util.Clock
import com.aut.ceit.clustering.mrssl.spatial.rdd.PairwisePartitioner

import org.apache.commons.math3.filter.MeasurementModel
import org.apache.commons.math3.ml.distance.DistanceMeasure
import org.apache.hadoop.fs.Path

object MRSSLDriver extends Logging {

  def main(args: Array[String]): Unit = {

    val commandLine = new CommandLine(args)

    val totalClock = new Clock()

    val debugPath = if (commandLine.hasOption("debugPath")) commandLine.getOptionValue("debugPath") else DebugHelper.defaultDebugPath
    DebugHelper.debugPath = debugPath
    DebugHelper.deletePreDebugDir

    val master = commandLine.getOptionValue("master") 
    val conf = new SparkConf()
      .setMaster(master)
      .setAppName("MRSSL")
      .set("spark.executor.memory", "5g")
      .set("spark.storage.memoryFraction", "0.02")
    if(commandLine.hasOption("maxCores")) conf.set("spark.cores.max", commandLine.getOptionValue("maxCores")) 

    val sc = new SparkContext(conf)

    var data: RDD[Point] = null
    val hasClusterLabel = commandLine.hasOption("hasClusterLabel")
    DebugHelper.logFile("hasClusterLabel=" + hasClusterLabel)

    if (hasClusterLabel)
      data = IOHelper.readDataset(sc, commandLine.getOptionValue("input"), true)
    else
      data = IOHelper.readDataset(sc, commandLine.getOptionValue("input"), false)

    var partitioningSettings = new PartitioningSettings()
    var mrsslSettings = new MRSSLSettings()

    if (commandLine.hasOption("numPoints"))
      partitioningSettings = partitioningSettings.withNumberOfPointsInBox(commandLine.getOptionValue("numPoints").toInt)
    if (commandLine.hasOption("epsilon"))
      mrsslSettings = mrsslSettings.withEpsilon(commandLine.getOptionValue("epsilon").toDouble)
    if (commandLine.hasOption("numClusters"))
      mrsslSettings = mrsslSettings.withNumClusters(commandLine.getOptionValue("numClusters").toInt)
    if(commandLine.hasOption("alphaFactor"))
      mrsslSettings = mrsslSettings.withAlphaFactor(commandLine.getOptionValue("alphaFactor").toDouble)
      
    val distMeasure = mrsslSettings.distanceMeasure

    //Phase 1: Partitioning the data with density boxes -----------------------------------------------------------------------------------
    val partitionedData = PointsPartitionedByBoxesRDD(data, partitioningSettings, mrsslSettings)

    //DEBUG
    ///*
    val rootBox = for (bound <- partitionedData.boundingBox.bounds) yield (bound.lower, bound.upper)
    DebugHelper.logFile("partitionedSize:" + partitionedData.partitions.length)
    DebugHelper.logFile("rootBox:" + rootBox.mkString(","))
    val boxBoundaries = partitionedData.boxes.map {
      box =>
        val s = for (bound <- box.bounds) yield (bound.lower, bound.upper)
        s.mkString(",")
    }
    sc.parallelize(boxBoundaries.toSeq, 1).coalesce(1, true).saveAsTextFile(DebugHelper.defaultBoxesPath)
    //*/

    //Phase 2: calculating dataSpheres ----------------------------------------------------------------------------------------------------
    //you can see this link for the co-location vs. co-partition rdds: https://groups.google.com/d/msg/spark-users/gUyCSoFo5RI/gSX95v3dCtEJ
    var dataSpheresRDD: RDD[DataSphere] = partitionedData.mapPartitionsWithIndex(
      (partitionIndex, it) =>
        {
          val offset = partitioningSettings.numberOfPointsInBox
          var dsIndex = partitionIndex * offset

          var dataSpheres = List[DataSphere]()

          val points = it.map(_._2)

          for (point <- points) {
            var isFollower = false
            for (ds <- dataSpheres; if !isFollower) {
              val distToLeader = distMeasure.compute(point.coordinates.toArray, ds.leader.coordinates.toArray)

              if (distToLeader <= mrsslSettings.epsilon) {
                isFollower = true
                ds.addFollower(distToLeader)
              }
            }

            if (!isFollower) {
              dataSpheres = new DataSphere(point, partitionIndex, dsIndex) :: dataSpheres
              dsIndex += 1
            }
          }

          dataSpheres.iterator
        }, true)
      .setName("Data Spheres")
      .cache() //Its incredible!!! It decreases the runtime unexpectedly!!! Hoooooooooooraaaaay!!!

      //Phase 3: Updating Mean, Standard Deviation, alpha and extend of each DS -------------------------------------------
      .mapPartitions(
        dataSphereIt => {
          var updatedDataSpheresList = List[DataSphere]()

          for (oldDataSphere <- dataSphereIt) {
            val updatedDataSphere = DataSphere.updateStatistics(oldDataSphere, mrsslSettings)
            updatedDataSpheresList = updatedDataSphere :: updatedDataSpheresList
          }

          // This is necessary to keep the order of dataSpheres in list as the same order they are generated,
          //so because the above for 'reverse' the dataSphere list, we should reverse it again to avoid any disorder in DSs.
          updatedDataSpheresList.reverse.iterator
        }, true)

    //DEBUG
    ///*
    dataSpheresRDD.coalesce(1, true).saveAsTextFile(DebugHelper.defaultDataSpheresPath)
    val dsWithExtendDebugRDD = dataSpheresRDD.map {
      ds =>
        ds.leader.coordinates.toArray.mkString(",") + "," + ds.extend
    }
      .coalesce(1, true).saveAsTextFile(DebugHelper.defaultDsWithExtendPath)
    //*/
    //DebugHelper.logFile("dataSpheresRDDCount:" + dataSpheresRDD.count())

    //Phase 4: Assign pFollower to each DS ----------------------------------------------------------------------------------------------------
    //TODO There should be a better way to avoid recalculating the assignment of each point to its leader
    var dsWithpFollowerRDD = dataSpheresRDD.zipPartitions(partitionedData, true)(
      (dataSpheresPartitionedIt, pointPartitionedIt) => {

        val pointPairsInPartition = pointPartitionedIt.toArray
        val pointsInPartition = pointPairsInPartition.map(_._2)
        val dataSpheresInPartition = dataSpheresPartitionedIt.toArray

        val pFollowerMap = scala.collection.mutable.Map[DataSphere, List[Point]]()

        for (dataSphere <- dataSpheresInPartition) pFollowerMap += (dataSphere -> List[Point]())

        for (point <- pointsInPartition) {

          var isFollower = false
          for (dataSphere <- dataSpheresInPartition; if !isFollower) {

            val distToLeader = distMeasure.compute(point.coordinates.toArray, dataSphere.leader.coordinates.toArray)
            if (distToLeader <= mrsslSettings.epsilon) { //point is a follower
              isFollower = true
              if (distToLeader >= dataSphere.extend) { //point is a pFollower also
                var pFollowerOfDS = pFollowerMap.get(dataSphere).get
                pFollowerOfDS = point :: pFollowerOfDS
                pFollowerMap(dataSphere) = pFollowerOfDS
              }
            }

          }
        }

        pFollowerMap.iterator
      })
      .setName("Data Sphere with pFollowers")
      .cache()

    //DEBUG
    ///*
    val dsWithpFollowerDebugRDD = dsWithpFollowerRDD.map {
      case (ds, pFollower) =>
        "Leader: " + ds.leader.coordinates.toArray.mkString(",") + "\t" + "numpFollower: " + pFollower.length
    }
      .coalesce(1, true).saveAsTextFile(DebugHelper.defaultpFollowerPath)
    //*/
    //Phase 5: Calculating Pairwise Distance of DataSpheres ------------------------------------------------------------------------------

    //Distributed approach
    val pairwiseDistanceRDD = calculatePairwiseDistance(dsWithpFollowerRDD, sc, distMeasure)

    //Single Machine approach
    //val dsWithpFollowerArray = dsWithpFollowerRDD.collect()
    //val pairwiseDistance = for(pair1 <- dsWithpFollowerArray; pair2 <- dsWithpFollowerArray if pair1._1 < pair2._1) yield ((pair1._1,pair2._1), dataSphereWithpFollowerDistance(pair1,pair2,distMeasure))

    //spark-based approach
    /*val pairwiseDistanceRDD = dsWithpFollowerRDD.cartesian(dsWithpFollowerRDD)
    .filter(pair => pair._1._1 < pair._2._1)
    .map(pair => ((pair._1._1, pair._2._1), dataSphereWithpFollowerDistance(pair._1, pair._2, distMeasure)))*/

    //DEBUG
    ///*
    val pairwiseDistanceDebugRDD = pairwiseDistanceRDD.map {
      case ((ds1, ds2), dist) => "((" + ds1.leader.coordinates.toArray.mkString(",") + "~~~" +
        ds2.leader.coordinates.toArray.mkString(",") + ") => " + dist + " )"
    }
      .coalesce(1, true).saveAsTextFile(DebugHelper.defaultPairwiseDistancePath)
    //*/
    //DebugHelper.logFile("pairwiseDistanceRDDCount:" + pairwiseDistanceRDD.count)

    //Phase 6: Do a Sequential HAC! Why sequential?! :( ---------------------------------------------------------------------------

    val dataSpheres = dataSpheresRDD.collect()
    val pairwiseDistance = pairwiseDistanceRDD.collect() //Distributed approach //Spark-based approach
    var clusteredDataSpheres = HAC.train(dataSpheres, pairwiseDistance, mrsslSettings)

    //Phase 7: Find Points that belong to their leader and label them with the label of their leader ------------------------------
    //We have 2 solution that each one needs to be tested and checked (We choose solution 1 here temporary!):
    //1- Create an RDD from clustered DSs and partition them with regarding to their original partitionID
    //2- Broadcast the clustered DSs to each node (I don't know how much it will cost actually!)

    val keyedClusteredDataSphere = for (cDS <- clusteredDataSpheres) yield (cDS.partitionIndex, cDS)
    val keyedClusteredDataSphereRDD = sc.parallelize(keyedClusteredDataSphere)
    val partitionedKeyedClusteredDataSphereRDD = new ShuffledRDD[Int, DataSphere, DataSphere](
      keyedClusteredDataSphereRDD,
      new DataSpherePartitioner(partitionedData.partitions.size))

    val clusteredDataRDD = partitionedData.zipPartitions(partitionedKeyedClusteredDataSphereRDD, true) {
      (pointsIt, clusteredDSIt) =>
        val pointsInPartition = pointsIt.map(_._2).toArray
        val dataSpheresInPartition = clusteredDSIt.map(_._2).toArray.sortWith(_ > _)

        var clusteredPoints = List[(Point, Int)]()

        for (point <- pointsInPartition) {

          var isFollower = false
          for (dataSphere <- dataSpheresInPartition; if !isFollower) {

            val distToLeader = distMeasure.compute(point.coordinates.toArray, dataSphere.leader.coordinates.toArray)
            if (distToLeader <= mrsslSettings.epsilon) { //point is a follower
              isFollower = true

              clusteredPoints = (point, dataSphere.calculatedClusterNum) :: clusteredPoints
            }

          }
          if (!isFollower) { // The point is NOISE
            clusteredPoints = (point, 0) :: clusteredPoints
          }
        }

        clusteredPoints.iterator
    }
    //DEBUG
    /*DebugHelper.logFile("clusteredDataRDDCount:" + clusteredDataRDD.count())*/
    val clusteredDataDebugRDD = clusteredDataRDD.map {
      x =>
        if (hasClusterLabel) x._1.coordinates.toArray.mkString(",") + "," + x._1.clusterId + "," + x._2
        else
          x._1.coordinates.toArray.mkString(",") + "," + x._2
    }
    .saveAsTextFile("hdfs://fnozarian1:9000/mrssl-debug")
      /*.coalesce(1, true).saveAsTextFile(DebugHelper.defaultClusteredDataPath).toString()*/

    totalClock.logTimeSinceStart("Totoal Time")
    sc.stop()
  }
  private def calculatePairwiseDistance(inputRDD: RDD[(DataSphere, List[Point])], sc: SparkContext, distanceMeasure: DistanceMeasure): RDD[((DataSphere, DataSphere), Double)] = {

    val numPartitions = inputRDD.partitions.size
    DebugHelper.logFile("numPartitions:" + numPartitions)

    //h is important parameter in grouping partitions together, however because of complexity its statically set to number of partitions
    var h = inputRDD.partitions.size
    val doubleH = h.toDouble

    require(h <= numPartitions)

    val e = numPartitions / h
    DebugHelper.logFile("e:" + e)

    var numShufflePartition = -1
    if ((numPartitions / doubleH) % 1 != 0)
      h = h + 1

    numShufflePartition = ((h * (h + 1)) / 2).toInt
    DebugHelper.logFile("numShufflePartition:" + numShufflePartition)

    require(numShufflePartition != -1)

    val broadcastParams = sc.broadcast((h, e))

    var pairwiseDistanceRDD = inputRDD.mapPartitionsWithIndex(

      (index, it) => {

        val params = broadcastParams.value
        val h = params._1
        val e = params._2

        var isFound = false
        var subsetIndex = -1
        for (i <- 0 to h - 1 if !isFound) {
          if (index >= i * e && index < (i + 1) * e) {
            subsetIndex = i
            isFound = true
          }
        }

        require(subsetIndex != -1)

        var keyedPartition = List[((Int, Int), (String, (DataSphere, List[Point])))]()

        val partitionElements = it.toArray
        //participating rows in each node
        for (j <- subsetIndex to h - 1) {
          for (element <- partitionElements) {
            keyedPartition = ((subsetIndex, j), ("r", element)) :: keyedPartition
          }
        }

        //participating columns in each node
        for (i <- 0 to subsetIndex) {
          for (element <- partitionElements) {
            keyedPartition = ((i, subsetIndex), ("c", element)) :: keyedPartition
          }
        }

        keyedPartition.iterator
      }, false)
      .groupByKey(new PairwisePartitioner(numShufflePartition))
      .mapPartitions(
        it => {

          val elems = it.flatMap(_._2).toArray

          val rowElems = elems.filter(_._1.equals("r")).map(_._2)
          val colElems = elems.filter(_._1.equals("c")).map(_._2)

          val pairwiseDistance = for (pair1 <- rowElems; pair2 <- colElems if pair1._1 < pair2._1) yield ((pair1._1, pair2._1), dataSphereWithpFollowerDistance(pair1, pair2, distanceMeasure))
          pairwiseDistance.iterator
        }, false)

    pairwiseDistanceRDD
  }
  private def dataSphereWithpFollowerDistance(dsWithpFollower1: (DataSphere, List[Point]), dsWithpFollower2: (DataSphere, List[Point]), distMeasure: DistanceMeasure): Double = {

    val leaderDistance = distMeasure.compute(dsWithpFollower1._1.leader.coordinates.toArray, dsWithpFollower2._1.leader.coordinates.toArray)

    var DSDistance: Double = -1
    val partDist = leaderDistance - (dsWithpFollower1._1.extend + dsWithpFollower2._1.extend)
    if (partDist >= 0) {
      DSDistance = Math.min(pFollowerDistance(dsWithpFollower1._2, dsWithpFollower2._2, distMeasure), partDist)
    } else {
      DSDistance = leaderDistance - Math.max(dsWithpFollower1._1.extend, dsWithpFollower2._1.extend)
    }

    DSDistance

  }
  private def pFollowerDistance(pFollowers1: List[Point], pFollowers2: List[Point], distMeasure: DistanceMeasure): Double = {

    var minDistance = Double.MaxValue
    for (p1 <- pFollowers1; p2 <- pFollowers2) {
      val pDist = distMeasure.compute(p1.coordinates.toArray, p2.coordinates.toArray)
      if (pDist < minDistance) minDistance = pDist
    }

    minDistance
  }

}