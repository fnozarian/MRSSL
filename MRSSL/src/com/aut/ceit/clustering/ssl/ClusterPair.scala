package com.aut.ceit.clustering.ssl

class ClusterPair(private var lCluster: Cluster,
                  private var rCluster: Cluster,
                  private var linkageDistance: Double) extends Ordered[ClusterPair] {

  def getOtherCluster(c: Cluster): Cluster = if (lCluster == c) rCluster else lCluster
  def getlCluster(): Cluster = lCluster
  def getrCluster(): Cluster = rCluster
  def setlCluster(lCluster: Cluster) = this.lCluster = lCluster
  def setrCluster(rCluster: Cluster) = this.rCluster = rCluster
  def getLinkageDistance(): Double = linkageDistance
  def setLinkageDistance(distance: Double) = this.linkageDistance = distance
  def reverse(): ClusterPair = new ClusterPair(getrCluster(), getlCluster(), getLinkageDistance())

  //change compare method to give a high priority to smaller distances (i.e. distance 2 is higher priority than 4)
  override def compare(that: ClusterPair): Int = {
    if (that == null) 1 //its first value was -1 // it was if (that == null || that.getLinkageDistance() ==null)
    else -(getLinkageDistance().compareTo(that.getLinkageDistance())) //the original one hasn't minus at first
  }

  def agglomerate: Cluster = {
    var name = "clstr#" + GlobalIndex.get
   
    var cluster = new DataSphere(name)
    cluster.setDistance(getLinkageDistance())
    cluster.addChild(lCluster)
    cluster.addChild(rCluster)
    lCluster.setParent(cluster)
    rCluster.setParent(cluster)

    cluster
  }

  override def toString(): String = {
    var sb: StringBuilder = new StringBuilder()
    if (lCluster != null) sb ++= lCluster.name
    if (rCluster != null) {
      if (sb.length > 0) { sb ++= " + " }
      sb ++= rCluster.name
    }
    sb ++= " : "
    sb.append(linkageDistance)

    sb.toString()
  }

  def hash: String = ClusterPair.hashCodePair(getlCluster(), getrCluster())

}

object ClusterPair {
  def hashCodePair(lCluster: Cluster, rCluster: Cluster): String = {
    hashCodePairNames(lCluster.name, rCluster.name)
  }

  private def hashCodePairNames(lName: String, rName: String): String = {
    if (lName.compareTo(rName) < 0)
      lName + "~~~" + rName
    else
      rName + "~~~" + lName
  }
}

object GlobalIndex {
  private var globalIndex: Int = 0

  def get = { globalIndex += 1; globalIndex }
}