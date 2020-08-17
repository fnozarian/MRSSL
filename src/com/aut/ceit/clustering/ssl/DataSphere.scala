package com.aut.ceit.clustering.ssl

import com.aut.ceit.clustering.mrssl.spatial.Point
import scala.collection.mutable.ArrayBuffer
import com.aut.ceit.clustering.mrssl.MRSSLSettings

/**
 * A data sphere (ds) is a summarized unit for a set of points (followers of a leader)
 *
 * @param leader The leader of the <i>ds</i>
 *
 */
class DataSphere(name: String) extends Cluster(name) with Serializable with Ordered[DataSphere] {

  def this(leader: Point) = { this(leader.pointId.toString()); this.leader = leader }

  def this(
    leader: Point,
    partitionIndex: Int,
    order: Double) {

    this(leader)
    this.order = order
    this.partitionIndex = partitionIndex
  }

  def this(oldDataSphere: DataSphere) {
    this(oldDataSphere.leader, oldDataSphere.partitionIndex, oldDataSphere.order)

    this.numFollower = oldDataSphere.numFollower
    this.ld = oldDataSphere.ld
    this.sd = oldDataSphere.sd

  }

  var leader: Point = null
  var partitionIndex = -1
  var order: Double = MRSSLSettings.UNORDERED

  private val distanceMeasure = MRSSLSettings.getDefaultDistanceMeasure

  var calculatedClusterNum = MRSSLSettings.UNCLASSIFIED

  /**
   * number of followers including the leader <i>l</i>
   */
  var numFollower: Int = 1

  /**
   * <b>sum</b> of d<sub>i</sub> from 1 to numFollower where d<sub>i</sub> is the distance from leader <i>l</i> to its ith follower
   */
  var ld: Double = 0
  /**
   * <b>sum square</b> of d<sub>i</sub> from 1 to numFollower where d<sub>i</sub> is the distance from leader <i>l</i> to its ith follower
   */
  var sd: Double = 0

  /**
   * the mean of points in data sphere. i.e. <b><i>ld</i> divide by <i>k</i></b>
   */
  var mean: Double = 0

  /**
   * the <b>standard deviation</b> of points in data sphere. i.e. <b> sqrt((sd/numFollower)-mean<sup>2</sup>)</b>
   */
  var standardDeviation: Double = 0

  //TODO it should be zero, but for test I adjust it to 0.5
  var alpha: Double = 0.5

  var extend: Double = 80

  def addFollower(distToLeader: Double) = {

    numFollower += 1
    ld += distToLeader
    sd += (distToLeader * distToLeader)

  }

  override def equals(that: Any): Boolean = {
    if (that.isInstanceOf[DataSphere]) {
      val ds = that.asInstanceOf[DataSphere]
      this.name.equals(ds.name)
    } else {
      false
    }
  }

  override def compare(that: DataSphere): Int = {
    this.order.compare(that.order)
  }

  override def toString(): String = {
    "(" + this.order + ") " + "\t" +
      "Name: " + this.name + "\t" +
      "Leader: " + this.leader.coordinates.toArray.mkString(",") + "\t" +
      "numFollower: " + this.numFollower + "\t" +
      "ld: " + this.ld + "\t" +
      "sd: " + this.sd + "\t" +
      "Mean: " + this.mean + "\t" +
      "Standard Deviation: " + this.standardDeviation + "\t" +
      "Alpha: " + this.alpha + "\t" +
      "Extend: " + this.extend
  }

  def setClusterNumAndFlattenLeafs(clusterNum: Int, flattenList: ArrayBuffer[DataSphere]) {

    this.calculatedClusterNum = clusterNum

    if (this.isLeaf()) flattenList += this
    else {
      getChildren().foreach { x => x.asInstanceOf[DataSphere].setClusterNumAndFlattenLeafs(clusterNum, flattenList) }
    }

  }

}
object DataSphere {

  def updateStatistics(oldDataSphere: DataSphere,mrsslSettings:MRSSLSettings): DataSphere = {
    val newDataSphere = new DataSphere(oldDataSphere)

    newDataSphere.mean = newDataSphere.ld / newDataSphere.numFollower
    newDataSphere.standardDeviation = Math.sqrt((newDataSphere.sd / newDataSphere.numFollower) - (newDataSphere.mean * newDataSphere.mean))
    //TODO update alpha
    if (newDataSphere.standardDeviation != 0)
      newDataSphere.alpha = mrsslSettings.alphaFactor * ((mrsslSettings.epsilon - newDataSphere.mean) / newDataSphere.standardDeviation)
    newDataSphere.extend = newDataSphere.mean + newDataSphere.alpha * newDataSphere.standardDeviation

    newDataSphere
  }
}