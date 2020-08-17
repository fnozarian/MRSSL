package com.aut.ceit.clustering.ssl

import scala.collection.mutable.ArrayBuffer
import com.aut.ceit.clustering.mrssl.MRSSLSettings

class HierarchicalBuilder(private var clusters: Array[DataSphere], private var distances: DistanceMap,mrsslSettings:MRSSLSettings) {

  def getDistances = distances
  def getClusters = clusters

  private val clustersBuffer = clusters.toBuffer

  def agglomerate(linkageStrategy: LinkageStrategy) {

    var minDistLink: Option[ClusterPair] = distances.removeFirst()

    if (minDistLink.isDefined) {

      val oldClusterL = minDistLink.get.getlCluster()

      val oldClusterR = minDistLink.get.getrCluster()

      val newCluster = minDistLink.get.agglomerate.asInstanceOf[DataSphere]
      
      clustersBuffer.remove(clustersBuffer.indexOf(oldClusterL))
      clustersBuffer.remove(clustersBuffer.indexOf(oldClusterR))

      for (iClust <- clustersBuffer) {
        var link1 = findByClusters(iClust, oldClusterL)
        var link2 = findByClusters(iClust, oldClusterR)
        var distanceValues = List[Double]()

        if (link1.isDefined) {
          var distVal: Double = link1.get.getLinkageDistance()
          distanceValues = distVal :: distanceValues
          var removed = distances.remove(link1.get)
        }

        if (link2.isDefined) {
          var distVal: Double = link2.get.getLinkageDistance()
          distanceValues = distVal :: distanceValues
          var removed = distances.remove(link2.get)
        }

        var newDistance = linkageStrategy.calculateDistance(distanceValues.toArray)
        var newLinkage: ClusterPair = new ClusterPair(iClust, newCluster, newDistance)
        distances.add(newLinkage)
      }
      clustersBuffer += newCluster
    }
  }

  private def findByClusters(c1: Cluster, c2: Cluster): Option[ClusterPair] = {
    distances.findByCodePair(c1, c2)
  }

  def isComplete(): Boolean = clustersBuffer.length == mrsslSettings.numClusters

  def getTopClusters() = clustersBuffer.toArray
  
}