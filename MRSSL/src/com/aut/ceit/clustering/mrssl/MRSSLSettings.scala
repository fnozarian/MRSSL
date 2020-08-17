package com.aut.ceit.clustering.mrssl

import org.apache.commons.math3.ml.distance.{ DistanceMeasure, EuclideanDistance }
class MRSSLSettings extends Serializable {
  private var _distanceMeasure: DistanceMeasure = MRSSLSettings.getDefaultDistanceMeasure
  private var _epsilon: Double = MRSSLSettings.getDefaultEpsilon
  private var _numClusters: Int = MRSSLSettings.getDefaultNumCluster
  private var _alphaFactor: Double = MRSSLSettings.getDefaultAlphaFactor
  /**
   * A distance measure
   *
   * @return
   */
  def distanceMeasure: DistanceMeasure = _distanceMeasure

  /**
   * Distance within which points are considered close enough to be assigned to one leader
   *
   * @return
   */
  def epsilon: Double = _epsilon

  /**
   * Specify the desired number of clusters. i.e tree cut-off number
   *
   * @return
   */
  def numClusters: Int = _numClusters
  
  /**
   * Specify the the tightness of spiral circle of pFollowers
   */
  def alphaFactor: Double = _alphaFactor

  /**
   * Set epsilon parameter of the algorithm (distance within which points are considered close enough to be assigned
   *  to one cluster)
   *
   * @param eps
   * @return
   */
  def withEpsilon(eps: Double) = {
    _epsilon = eps
    this
  }

  /**
   * Set the number of cluster returned by algorithm
   * @param numClusters
   * @return
   */
  def withNumClusters(numClusters: Int) = {
    _numClusters = numClusters
    this
  }
  
  def withAlphaFactor(alphaFactor: Double)={
    _alphaFactor = alphaFactor
    this
  }

  /**
   * Sets a distance measure
   *
   * @param dm An object which implements the
   *           [[org.apache.commons.math3.ml.distance.DistanceMeasure]] interface
   * @return This [[MRSSLSettings]] object with modified distance measure
   */
  def withDistanceMeasure(dm: DistanceMeasure) = {
    _distanceMeasure = dm
    this
  }
}
object MRSSLSettings {

  def getDefaultDistanceMeasure: DistanceMeasure = { new EuclideanDistance() }

  private def getDefaultAlphaFactor = 0.95
  private def getDefaultEpsilon: Double = 100 
  private def getDefaultNumCluster: Int = 2 

  def UNCLASSIFIED: Int = -2
  def UNORDERED = -2
}