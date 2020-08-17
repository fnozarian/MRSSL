package com.aut.ceit.clustering

import org.apache.spark.rdd.RDD
import scala.collection.mutable.WrappedArray.ofDouble
import org.apache.commons.math3.ml.distance.EuclideanDistance
import com.aut.ceit.clustering.mrssl.spatial.{PointSortKey, Point}

/** Contains implementation of distributed MRSSL algorithm as well as tools for exploratory analysis.
  *
  *
  */
package object mrssl{

  /** Represents one record in a dataset
    *
    */
  type PointCoordinates = ofDouble

  /** Represents a dataset which needs to be clustered
    *
    */
  type RawDataSet = RDD[Point]

  /** Unique point ID in a data set
   *
   */
  private [mrssl] type PointId = Long

  private [mrssl] type TempPointId = Int

  /** Unique id of a box-shaped region in a data set
   *
   */
  private [mrssl] type BoxId = Int

  /** Cluster ID
   *
   */
  type ClusterId = Long

  /** A pair of IDs of density-based partitions adjacent to each other
   *
   */
  private [mrssl] type PairOfAdjacentBoxIds = (BoxId, BoxId)
}
