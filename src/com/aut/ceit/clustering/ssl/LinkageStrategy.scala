package com.aut.ceit.clustering.ssl

trait LinkageStrategy {

  //I have changed the 'Collection<Distance>' to 'Array[Distance]'
  def calculateDistance(distances: Array[Double]): Double

}