package com.aut.ceit.clustering.mrssl.util

import org.apache.spark.Logging

private[mrssl] class Clock extends Logging {
  val startTime = System.currentTimeMillis()

  def logTimeSinceStart(): Unit = {
    logTimeSinceStart("Test")
  }

  def logTimeSinceStart(message: String) = {
    val currentTime = System.currentTimeMillis()
    val timeSinceStart = (currentTime - startTime) / 1000.0

    logInfo(s"$message took $timeSinceStart seconds")
  }
  def log(msg:String){
    
    logInfo(msg)
  }
}
