package com.aut.ceit.clustering.mrssl.spatial.rdd

import org.apache.spark.Partitioner

class PairwisePartitioner(numPartition:Int) extends Partitioner {

  override def numPartitions = numPartition

  override def getPartition(key: Any): Int = key match { // (0,0) => 0 , (0,1) => 1 , (0,2) => 3 , (1,1) => 2 , (1,2) => 4 , (2,2) => 5
    case (r: Int, c: Int) => { println((r, c)); ((c * (c + 1)) / 2) + r; }

  }

  override def equals(other: Any): Boolean = other match {
    case pwPartitioner: PairwisePartitioner =>
      pwPartitioner.numPartitions == numPartition
    case _ =>
      false
  }
}