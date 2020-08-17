package com.aut.ceit.clustering.mrssl.spatial.rdd

import org.apache.spark.Partitioner
import com.aut.ceit.clustering.ssl.DataSphere

class DataSpherePartitioner(numPartition:Int) extends Partitioner {

  override def numPartitions = numPartition
  
  override def getPartition(key:Any):Int = key match {
    case index:Int => index 
  }
  
  override def equals(other:Any):Boolean= other match{
    case dsPartitioner: DataSpherePartitioner => 
      dsPartitioner.numPartitions == numPartition
    case _ =>
      false
  }
  
}