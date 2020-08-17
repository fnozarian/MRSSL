package com.aut.ceit.clustering.ssl
class SingleLinkageStrategy extends LinkageStrategy{
  
   override def calculateDistance(distances: Array[Double]): Double ={
    var min: Double = Double.NaN
    for(dist <- distances){
      if(min.isNaN() || dist < min) min = dist
    }
    
    min
  }
}