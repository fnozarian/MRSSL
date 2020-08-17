package com.aut.ceit.clustering.mrssl.spatial

import com.aut.ceit.clustering.mrssl._

/**
 * Represents a point in multi-dimensional space and metadata required by the distributed MRSSL algorithm
 *
 * @param coordinates Point's coordinates
 * @param pointId A unique identifier of the point
 * @param boxId An identifier of a partition of a data set which this point belongs to
 * @param distanceFromOrigin Distance of this point from origin
 * @param clusterId ID of a cluster which this points belongs to
 */
class Point(
  val coordinates: PointCoordinates,
  val pointId: PointId = 0,
  val boxId: BoxId = 0,
  val distanceFromOrigin: Double = 0.0,
  val clusterId: ClusterId = MRSSLModel.UndefinedCluster) extends Serializable with Ordered[Point] {

  def this(coords: Array[Double]) = this(new PointCoordinates(coords))

  def this(pt: Point) = this(pt.coordinates, pt.pointId, pt.boxId, pt.distanceFromOrigin,
    pt.clusterId)

  def this(coords: Double*) = this(new PointCoordinates(coords.toArray))

  def withPointId(newId: PointId) = {
    new Point(this.coordinates, newId, this.boxId, this.distanceFromOrigin,
      this.clusterId)
  }

  def withBoxId(newBoxId: BoxId) = {
    new Point(this.coordinates, this.pointId, newBoxId, this.distanceFromOrigin,
      this.clusterId)
  }

  def withDistanceFromOrigin(newDistance: Double) = {
    new Point(this.coordinates, this.pointId, this.boxId, newDistance,
      this.clusterId)
  }

  def withClusterId(newId: ClusterId) = {
    new Point(this.coordinates, this.pointId, this.boxId, this.distanceFromOrigin, newId)
  }

  override def equals(that: Any): Boolean = {

    if (that.isInstanceOf[Point]) {
      that.asInstanceOf[Point].canEqual(this) &&
        this.coordinates == that.asInstanceOf[Point].coordinates // We take only coordinates into account
      // and don't care about other attributes
    } else {
      false
    }
  }

  override def hashCode(): Int = {
    coordinates.hashCode() // We take only coordinates into account
    // and don't care about other attributes
  }

  override def toString(): String = {
    "Point at (" + coordinates.mkString(", ") + "); id = " + pointId + "; box = " + boxId +
      "; cluster = " + clusterId + ";"
  }

  def canEqual(other: Any) = other.isInstanceOf[Point]

  override def compare(that: Point): Int = {
    var result = 0
    var i = 0

    while (result == 0 && i < coordinates.size) {
      result = this.coordinates(i).compareTo(that.coordinates(i))
      i += 1
    }

    result
  }
}
