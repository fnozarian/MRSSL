package com.aut.ceit.clustering.mrssl.spatial.rdd

import org.apache.spark.Partitioner
import com.aut.ceit.clustering.mrssl.spatial.{PointSortKey, Point, Box}
import com.aut.ceit.clustering.mrssl.BoxId

/** A partitioner which assigns each entry in a dataset to a [[com.aut.ceit.clustering.mrssl.spatial.Box]]
  *
  * @param boxes A collection of [[com.aut.ceit.clustering.mrssl.spatial.Box]]es
  */
private [mrssl] class BoxPartitioner (val boxes: Iterable[Box]) extends Partitioner {

  assert (boxes.forall(_.partitionId >= 0))

  private val boxIdsToPartitions = generateBoxIdsToPartitionsMap(boxes)

  override def numPartitions: Int = boxes.size

  def getPartition(key: Any): Int = {

    key match {
      case k: PointSortKey => boxIdsToPartitions(k.boxId)
      case boxId: BoxId => boxIdsToPartitions(boxId)
      case pt: Point => boxIdsToPartitions(pt.boxId)
      case _ => 0 // throw an exception?
    }
  }


  private def generateBoxIdsToPartitionsMap (boxes: Iterable[Box]): Map[BoxId, Int] = {
    boxes.map ( x => (x.boxId, x.partitionId)).toMap
  }
}

private [mrssl] object BoxPartitioner {

  def assignPartitionIdsToBoxes (boxes: Iterable[Box]): Iterable[Box] = {
    boxes.zip (0 until boxes.size).map ( x => x._1.withPartitionId(x._2) )
  }

}