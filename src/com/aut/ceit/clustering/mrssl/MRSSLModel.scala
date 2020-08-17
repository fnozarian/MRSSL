package com.aut.ceit.clustering.mrssl

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext._
import com.aut.ceit.clustering.mrssl.spatial.Point
import com.aut.ceit.clustering.mrssl.spatial.rdd.PointsPartitionedByBoxesRDD

/** Represents results calculated by MRSSL algorithm.
  *
  * You cannot instantiate it directly
  */
class MRSSLModel private[mrssl] (val allPoints: RDD[Point], 
val settings: MRSSLSettings)
  extends Serializable {



  /** 
    * Predicts which cluster a point would belong to
    * 
    * @param newPoint A [[com.aut.ceit.clustering.mrssl.PointCoordinates]] for which you want to make a prediction
    * @return If the point can be assigned to a cluster, then this cluster's ID is returned.
    *         
    */
//  def predict (newPoint: Point): ClusterId = {
//   
//  }
  
}

/** Contains constants which designate cluster ID
  *
  */
object MRSSLModel {

  /** Initial value for cluster ID of each point.
    *
    */
  private [mrssl] val UndefinedCluster: ClusterId = -2
}
