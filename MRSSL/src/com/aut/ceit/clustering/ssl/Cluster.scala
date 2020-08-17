package com.aut.ceit.clustering.ssl

import scala.collection.mutable.ArrayBuffer
import scala.Equals
import java.io.Serializable

class Cluster(val name:String) extends Serializable {
  
 
  private var parent: Option[Cluster] = None
  private var children: Option[ArrayBuffer[Cluster]] = None
  private var distance: Double = -1

  def getDistance() = distance

  def getDistanceValue() = distance

  

  def setDistance(distance: Double): this.type = {
    this.distance = distance
    this
  }

  def getChildren(): ArrayBuffer[Cluster] = {
    if (!children.isDefined) children = Some(new ArrayBuffer[Cluster])
    children.get
  }

  def setChildren(children: ArrayBuffer[Cluster]): this.type = {
    this.children = Some(children)
    this
  }

  def getParent(): Option[Cluster] = parent

  def setParent(parent: Cluster): this.type = {
    this.parent = Some(parent)
    this
  }

  def addChild(cluster: Cluster) = getChildren().append(cluster)

  def contains(cluster: Cluster): Boolean = getChildren().contains(cluster)

  override def toString() = {
    val cname = if (name != null) name else "?"
    "Cluster " + cname
  }

  override def equals(that: Any): Boolean = {
    if (that == null) return false
    if (this eq that.asInstanceOf[AnyRef]) return true
    if (this.getClass() != that.getClass()) return false
    if (name != null && (that.asInstanceOf[Cluster].name == null)) return false
    else if (!name.equals(that.asInstanceOf[Cluster].name)) return false
    true
  }

  override def hashCode(): Int = { if (name == null) 0 else name.hashCode() }

  def isLeaf(): Boolean = getChildren().size == 0

  def countLeafs(): Int = countLeafs(this, 0)

  private def countLeafs(node: Cluster, count: Int): Int = {
    var newCount = count
    if (node.isLeaf()) newCount += 1
    for (child <- getChildren()) {
      newCount += child.countLeafs()
    }
    newCount
  }

  def getTotalDistance(): Double = {
    var dist: Double = if (getDistance() == -1) 0 else getDistance()
    if (getChildren().size > 0) {
      dist += children.get(0).getTotalDistance()
    }
    dist
  }

  def print() {
    print("", true);
  }

  def print(prefix: String, isTail: Boolean) {

    var trailString = if (isTail) "└── " else "├── "

    if (this.isInstanceOf[DataSphere] && this.asInstanceOf[DataSphere].leader != null) {
      val ds = this.asInstanceOf[DataSphere]
      println(prefix + trailString + ds.leader.coordinates.toArray.mkString(",") + "(" + ds.calculatedClusterNum + ")");
    } else
      println(prefix + trailString + name);

    var children = getChildren()
    var trailStringChild = if (isTail) "    " else "│   "
    for (i <- 0 until children.size - 1) children(i).print(prefix + trailStringChild, false)

    if (children.size > 0) children(children.size - 1).print(prefix + trailStringChild, true);

  }

}