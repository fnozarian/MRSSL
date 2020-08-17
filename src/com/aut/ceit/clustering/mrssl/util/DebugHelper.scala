package com.aut.ceit.clustering.mrssl.util

import org.apache.spark.SparkContext
import org.apache.hadoop.fs.Path
import org.apache.commons.io.FileUtils
import org.apache.spark.rdd.RDD
import scala.reflect.ClassTag
import org.apache.spark.Logging

private[mrssl] object DebugHelper extends Logging {

  val defaultDebugPath = new String("/home/fnozarian/mrssl-debug")
  var debugPath = defaultDebugPath

  val defaultLogPath = debugSubPath("log")
  var logPath = defaultLogPath

  val defaultBoxesPath = debugSubPath("Boxes")
  val defaultDsWithExtendPath = debugSubPath("DsWithExtend")
  val defaultDataSpheresPath = debugSubPath("DataSpheres")
  val defaultPairwiseDistancePath = debugSubPath("PairwiseDistance")
  val defaultClusteredDataPath = debugSubPath("ClusteredData")
  val defaultpFollowerPath = debugSubPath("pFollower")
  val defaultPointInBoxes = debugSubPath("PointInBoxes")
  
  def deletePreDebugDir = FileUtils.deleteDirectory(new java.io.File(debugPath))

  def printRDD[T: ClassTag](rdd: RDD[T], relativePath: String, singleFile: Boolean) {

    val path = new Path(debugPath, relativePath).toString()

    if (singleFile)
      rdd.coalesce(1, true).saveAsTextFile(path)
    else
      rdd.saveAsTextFile(path)
  }

  def debug(msg: String): Unit = {
    logInfo(msg)
  }

  def logFile(msg: String) {
    val debugFolder = new java.io.File(debugPath)
    if (!debugFolder.exists())
      debugFolder.mkdir()

    logError("logPath:" + logPath)
    var logFile = new java.io.File(logPath)
    if (!logFile.exists())
      logFile.createNewFile()

    val fw = new java.io.FileWriter(logFile, true)
    fw.write(msg + "\n")
    fw.close();
  }
  def debugSubPath(subname: String): String = {
    new Path(debugPath, subname).toString()
  }

}
