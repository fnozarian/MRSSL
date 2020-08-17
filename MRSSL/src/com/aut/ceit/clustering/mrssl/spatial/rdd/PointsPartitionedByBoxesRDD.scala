package com.aut.ceit.clustering.mrssl.spatial.rdd

import com.aut.ceit.clustering.mrssl._
import com.aut.ceit.clustering.mrssl.spatial.{BoxCalculator, PointSortKey, Box, Point}
import org.apache.spark.rdd.{ShuffledRDD, RDD}
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import com.aut.ceit.clustering.mrssl.util.PointIndexer
import org.apache.hadoop.fs.Path
import com.aut.ceit.clustering.mrssl.util.DebugHelper

/** Density-based partitioned RDD where each point is accompanied by its sort key
  *
  * @param prev
  * @param boxes
  * @param boundingBox
  */
private [mrssl] class PointsPartitionedByBoxesRDD  (prev: RDD[(PointSortKey, Point)], val boxes: Iterable[Box], val boundingBox: Box)
  extends ShuffledRDD [PointSortKey, Point, Point] (prev, new BoxPartitioner(boxes))

object PointsPartitionedByBoxesRDD {

  def apply (rawData: RawDataSet,
    partitioningSettings: PartitioningSettings = new PartitioningSettings (),
    mrsslSettings: MRSSLSettings = new MRSSLSettings ())
    : PointsPartitionedByBoxesRDD = {

    val sc = rawData.sparkContext
    val boxCalculator = new BoxCalculator (rawData)
    val (boxes, boundingBox) = boxCalculator.generateDensityBasedBoxes (partitioningSettings, mrsslSettings)
    
    val broadcastBoxes = sc.broadcast(boxes)
    var broadcastNumberOfDimensions = sc.broadcast (boxCalculator.numberOfDimensions)

    val pointsInBoxes = PointIndexer.addMetadataToPoints(
      rawData,
      broadcastBoxes,
      broadcastNumberOfDimensions,
      mrsslSettings.distanceMeasure)
      /*pointsInBoxes.map( point => {
        (point._1.boxId,1)
      }).reduceByKey(_+_).coalesce(1, false).saveAsTextFile(DebugHelper.defaultPointInBoxes)*/
      
    PointsPartitionedByBoxesRDD (pointsInBoxes, boxes, boundingBox)
  }

  def apply (pointsInBoxes: RDD[(PointSortKey, Point)], boxes: Iterable[Box], boundingBox: Box): PointsPartitionedByBoxesRDD = {
    new PointsPartitionedByBoxesRDD(pointsInBoxes, boxes, boundingBox)
  }


  private [mrssl] def extractPointIdsAndCoordinates (data: RDD[(PointSortKey, Point)]): RDD[(PointId, PointCoordinates)] = {
    data.map ( x => (x._2.pointId, x._2.coordinates) )
  }

}


