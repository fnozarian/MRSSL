package com.aut.ceit.clustering.mrssl.spatial

import com.aut.ceit.clustering.mrssl._

private [mrssl] class BoxIdGenerator (val initialId: BoxId) {
  var nextId = initialId

  def getNextId (): BoxId = {
    nextId += 1
    nextId
  }
}
