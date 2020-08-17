package com.aut.ceit.clustering.mrssl.util

import org.apache.spark.SparkContext
import com.aut.ceit.clustering.mrssl.{ MRSSLModel, RawDataSet, ClusterId }
import org.apache.spark.rdd.RDD
import com.aut.ceit.clustering.mrssl.spatial.Point
import scala.collection.mutable.WrappedArray.ofDouble

/**
 * Contains functions for reading and writing data
 *
 */
object IOHelper {

  /**
   *  Reads a dataset from a CSV file. That file should contain double values separated by commas
   *
   * @param sc A SparkContext into which the data should be loaded
   * @param path A path to the CSV file
   * @return A [[com.aut.ceit.clustering.mrssl.RawDataSet]] populated with points
   */
  def readDataset(sc: SparkContext, path: String,hasClusterLabel: Boolean = false): RawDataSet = {
    val rawData = sc.textFile(path)

    var returnedDataset:RawDataSet = null
    
    if(hasClusterLabel){
      returnedDataset = rawData.map(
      line => {
        val splitedLine = line.split(separator)
        
        val clusterLabel = splitedLine.last.replaceAll("^\"|\"$", "").toLong
        
        new Point(splitedLine.dropRight(1).map(_.toDouble)).withClusterId(clusterLabel)
      })
    }else{
      returnedDataset = rawData.map(
      line => {
        new Point(line.split(separator).map(_.toDouble))
      })  
    }
    
      
      returnedDataset
  }

  /**
   * Saves clustering result into a CSV file. The resulting file will contain the same data as the input file,
   * with a cluster ID appended to each record. The order of records is not guaranteed to be the same as in the
   * input file
   *
   * @param model A [[com.aut.ceit.clustering.mrssl.MRSSLModel]] obtained from MRSSL.train method
   * @param outputPath Path to a folder where results should be saved. The folder will contain multiple
   *                   partXXXX files
   */
  def saveClusteringResult(model: MRSSLModel, outputPath: String) {

    model.allPoints.map(pt => {

      pt.coordinates.mkString(separator) + separator + pt.clusterId
    }).saveAsTextFile(outputPath)
  }

  private[mrssl] def saveTriples(data: RDD[(Double, Double, Long)], outputPath: String) {
    data.map(x => x._1 + separator + x._2 + separator + x._3).saveAsTextFile(outputPath)
  }

  private def separator = ","

}
