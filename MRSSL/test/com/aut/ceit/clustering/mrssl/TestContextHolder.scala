package com.aut.ceit.clustering.mrssl

import org.apache.spark.SparkContext

object TestContextHolder {
  val sc = new SparkContext ("local[8]", "test")
}
