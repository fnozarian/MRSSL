package com.aut.ceit.clustering.ssl
import org.apache.spark.mllib.linalg.Matrices
import scala.collection.mutable.ArrayBuffer
import breeze.linalg.norm
import breeze.linalg.{ Vector => BV }
import com.aut.ceit.clustering.mrssl.MRSSLSettings

object HAC {

  def train(dataSpheres: Array[DataSphere], pairwiseDistance: Array[((DataSphere, DataSphere), Double)],mrsslSettings:MRSSLSettings): Array[DataSphere] = {

    var hierarchicalAlg = new DefaultClusteringAlgorithm()
    var topClusters = hierarchicalAlg.performClustering(dataSpheres, pairwiseDistance, new SingleLinkageStrategy,mrsslSettings)

    var i = 1
    val flattenDSClusters = ArrayBuffer[DataSphere]()
    for (topCluster <- topClusters) {
      topCluster.asInstanceOf[DataSphere].setClusterNumAndFlattenLeafs(i, flattenDSClusters)
      i += 1
    }
    
    flattenDSClusters.toArray
  }

}