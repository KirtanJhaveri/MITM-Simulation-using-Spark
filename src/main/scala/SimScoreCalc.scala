object SimScoreCalc{
  private def jaccardSimilarity[T](set1: Set[T], set2: Set[T]): Double = {
    val intersectionSize = set1.intersect(set2).size.toDouble
    val unionSize = set1.union(set2).size.toDouble
    if (unionSize == 0) 0.0 else intersectionSize / unionSize
  }

  def calculateJaccardIndex(node1: NodeObject, node2: NodeObject): Double = {
    val attributes = Seq(
      (node1.field1, node2.field1),
      (node1.field2, node2.field2),
      (node1.field3, node2.field3),
      (node1.field4, node2.field4),
      (node1.field5, node2.field5),
      (node1.field6, node2.field6),
      (node1.field7, node2.field7),
      (node1.field8, node2.field8),
      (node1.field9, node2.field9)
    )

    val jaccardIndices = attributes.map { case (attr1, attr2) =>
      val intersection = Set(attr1).intersect(Set(attr2))
      val union = Set(attr1).union(Set(attr2))
      val jaccardIndex = jaccardSimilarity(intersection, union)
//      println(s"Attr1: $attr1, Attr2: $attr2, Intersection: $intersection, Union: $union, Jaccard Index: $jaccardIndex")
      jaccardIndex
    }

    // Calculate the average Jaccard index for all attributes
    val averageJaccardIndex = jaccardIndices.sum / jaccardIndices.size.toDouble
//    println(s"Average Jaccard Index: $averageJaccardIndex")
    averageJaccardIndex

  }

}