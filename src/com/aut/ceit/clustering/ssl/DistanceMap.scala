package com.aut.ceit.clustering.ssl

import scala.collection.mutable.HashMap
import scala.collection.mutable.PriorityQueue
import scala.collection.mutable.ArrayBuffer

/**
 * @author Farzad Nozarian
 */
class DistanceMap {

  private val pairHash: HashMap[String, ClusterPair] = HashMap()
  private val data: PriorityQueue[ClusterPair] = PriorityQueue()

  def list(): Seq[ClusterPair] = {
    val l: ArrayBuffer[ClusterPair] = new ArrayBuffer[ClusterPair]
    for (clusterPair <- data) l += clusterPair
    l
  }

  def findByCodePair(c1: Cluster, c2: Cluster): Option[ClusterPair] = {
    val inCode = ClusterPair.hashCodePair(c1, c2)
    if (pairHash.get(inCode).isDefined) pairHash.get(inCode) else None
  }

  def removeFirst(): Option[ClusterPair] = {
    var poll: Option[ClusterPair] = try { Some(data.dequeue()) } catch { case e: NoSuchElementException => None }

    while (poll.isDefined && !pairHash.contains(poll.get.hash)) poll = try { Some(data.dequeue()) } catch { case e: NoSuchElementException => None }

    if (poll.isDefined) pairHash.remove(poll.get.hash)

    poll
  }

  def remove(link: ClusterPair): Boolean = {
    var remove1: Option[ClusterPair] = pairHash.remove(link.hash)
    if (!remove1.isDefined) false else true
  }

  def add(link: ClusterPair): Boolean = {
    var existingLink = pairHash.get(link.hash)
    if (existingLink.isDefined) { throw new Exception("Adding redundant link: " + existingLink.get.hash); false }
    else {
      pairHash.put(link.hash, link)
      data.enqueue(link)
      true
    }
  }

}