package com.aut.ceit.clustering.mrssl.spatial.rdd

/** Represents density-based partitioning settings. Most of the time you can use default settings,
  * or adjust only the numberOfPointsInBox parameter
  *
  * During density-based partitioning, a data set is split along the longest dimension into numberOfSplits parts,
  * then each part is split again and so on, until the maximum number of levels ( numberOfLevels ) is reached,
  * or until the number of points in this part of the data set is less than a specified number ( numberOfPointsInBox )
  *
  * @param numberOfSplits A number of parts at each level
  * @param numberOfLevels Number of levels
  * @param numberOfPointsInBox APPROXIMATE number of points in each density-based partition. The actual number can be
  *                            anywhere from 0 to 2*numberOfPointsInBox-1, depending on distribution of your data.
  */
class PartitioningSettings  (
     val numberOfSplits: Int = PartitioningSettings.DefaultNumberOfSplitsAlongEachAxis,
     val numberOfLevels: Int = PartitioningSettings.DefaultNumberOfLevels,
     val numberOfPointsInBox: Long = PartitioningSettings.DefaultNumberOfPointsInBox
    )
  extends Serializable {

  def withNumberOfLevels (nl: Int): PartitioningSettings = {
    new PartitioningSettings(this.numberOfSplits, nl, this.numberOfPointsInBox)
  }
  
  def withNumberOfPointsInBox(np:Int):PartitioningSettings ={
    new PartitioningSettings(this.numberOfSplits,this.numberOfLevels,np)
  }
}

object PartitioningSettings {

  def DefaultNumberOfSplitsAlongEachAxis: Int = 2

  def DefaultNumberOfLevels: Int = 10

  def DefaultNumberOfPointsInBox: Long = 10000

}
