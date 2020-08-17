
package com.aut.ceit.clustering.ssl

import org.apache.spark.mllib.linalg.Matrix
import breeze.linalg.{ Vector => BV }
import com.aut.ceit.clustering.mrssl.MRSSLSettings

trait ClusteringAlgorithm {

  //change matrix of 2D double e.g. double[][] distances to linalg Matrix
  def performClustering(dataSpheres: Array[DataSphere],pairwiseDistance: Array[((DataSphere, DataSphere), Double)],linkageStrategy: LinkageStrategy,mrsslSettings:MRSSLSettings): Array[DataSphere]  
}