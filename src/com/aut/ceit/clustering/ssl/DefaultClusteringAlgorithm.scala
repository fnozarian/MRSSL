package com.aut.ceit.clustering.ssl

import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.HashMap
import breeze.linalg.norm
import breeze.linalg.{ Vector => BV }
import org.apache.spark.mllib.linalg.Matrix
import com.aut.ceit.clustering.mrssl._

class DefaultClusteringAlgorithm extends ClusteringAlgorithm {

  override def performClustering(dataSpheres: Array[DataSphere], pairwiseDistance: Array[((DataSphere, DataSphere), Double)], linkageStrategy: LinkageStrategy,mrsslSettings:MRSSLSettings): Array[DataSphere] = {
    checkArguments(pairwiseDistance, linkageStrategy)

    /*Setup Model*/
    val clustersMap = createClustersMap(dataSpheres)
    val linkages = createLinkages(pairwiseDistance,clustersMap)
    val clusters = clustersMap.values.toArray
    
    val builder: HierarchicalBuilder = new HierarchicalBuilder(clusters, linkages,mrsslSettings)
    while (!builder.isComplete()) builder.agglomerate(linkageStrategy)

    builder.getTopClusters()
    }

  def checkArguments(data: Array[((DataSphere, DataSphere), Double)], linkageStrategy: LinkageStrategy) = {
    if (data == null || data.length == 0) throw new IllegalArgumentException("Invalid distance Matrix")
    if (linkageStrategy == null) throw new IllegalArgumentException("Undefined linkage strategy")
  }

  private def createClustersMap(dataSpheres: Array[DataSphere]): HashMap[Long,DataSphere] = {
    
    val dsClustersMap = HashMap[Long,DataSphere]()
    
    for (i <- 0 until dataSpheres.length) 
      dsClustersMap += (dataSpheres(i).name.toLong -> dataSpheres(i))
    
    dsClustersMap
  }

  private def createLinkages(data: Array[((DataSphere, DataSphere), Double)], dsClustersMap: HashMap[Long,DataSphere]): DistanceMap = {
    var linkage: DistanceMap = new DistanceMap()

    for (dsPair <- data) {
      val lCluster = dsClustersMap(dsPair._1._1.name.toLong) 
      val rCluster = dsClustersMap(dsPair._1._2.name.toLong)
      val pairDistance: Double = dsPair._2
      val link = new ClusterPair(lCluster, rCluster, pairDistance)
      linkage.add(link)
    }

    linkage
  }

}