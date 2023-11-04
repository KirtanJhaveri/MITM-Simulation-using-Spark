import NetGraphAlgebraDefs.NetGraph
import NetGraphAlgebraDefs.Action
import org.apache.spark.graphx.{Edge, VertexId}
import org.apache.spark.rdd.RDD
//import org.slf4j.LoggerFactory
import org.apache.spark.SparkContext
import scala.io.Source



object PreProcessor {
//  private val logger = LoggerFactory.getLogger(getClass)

  def parseNodeObject(input: String): NodeObject = {
//    println("input")
//    println(input)
    val pattern = "NodeObject\\((\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+\\.\\d+),(.+)\\)".r
    input match {
      case pattern(id, a, b, c, d, e, f, g, h, i) =>
        NodeObject(id.toInt, a.toInt, b.toInt, c.toInt, d.toInt, e.toInt, f.toInt, g.toInt, h.toDouble, i.toBoolean)
      case _ => throw new IllegalArgumentException("cannot parse please check input format!")
    }
  }
  def preprocessGraph(outputDirectory: String, sc: SparkContext): (RDD[(VertexId, NodeObject)], RDD[Edge[String]]) = {

//    val outputDirectory = args(0)


//    //    val getOriginalNodes = originalGraph.head.sm.nodes().toString
//    val getOriginalNodes = loadPerturbedNodes.toString()
//    //    println(getOriginalNodes)
//    //    println("originalNodes: ", getOriginalNodes)


    val graph2 = NetGraph.load(outputDirectory)
////    println(graph2)
    val edges = graph2.head.sm.edges().toString
////    println(edges)
    val nodes = graph2.head.sm.nodes().toString
////    println(nodes)
    val processed_nodes = nodes.drop(1).dropRight(1).split(", ")
    val processed_edges = edges.dropRight(2).drop(2).split(">, <").map(_.trim)
//    processed_nodes.foreach(println)
//    val filePath = "inputGraphs/20_nodes_perturbed.txt"
//    val bufferedSource = Source.fromFile(filePath)
//    // Read lines from the file and convert them to an array of strings
//    val fileContents: Array[String] = bufferedSource.getLines.toArray
//    // Close the buffered source to release resources
//    bufferedSource.close()
//    if (fileContents sameElements processed_nodes)
//      println("you might have a chance")
//    else
//      println("loser")

//    val filePath1 = "inputGraphs/20_nodes_edges_perturbed.txt"
//    val bufferedSource1 = Source.fromFile(filePath1)
//    // Read lines from the file and convert them to an array of strings
//    val fileContents1: Array[String] = bufferedSource1.getLines.toArray
//    // Close the buffered source to release resources
//    bufferedSource1.close()
//    if (fileContents1 sameElements processed_edges)
//      println("you might have a chance")
//    else
//      println("loser")


    //    val NodeObjectPreProcess = processed_nodes.map(parseNodeObject).toList
    val NodeObjectPreProcess = processed_nodes.map(parseNodeObject).toList
    val nodesRdd: RDD[(VertexId,NodeObject)]= sc.parallelize(NodeObjectPreProcess).map{
      node=>(node.nodeId,node)
    }
//    val collectedNodes = nodesRdd.collect()
//    println("Nodes RDD:")
//    collectedNodes.foreach(println)


//    processed_edges.foreach(println)

    val EdgeObjectPreProcess: List[(NodeObject, NodeObject)] = processed_edges.map { line =>
      val nodes = line.split(" -> ")
      val node1 = parseNodeObject(nodes(0))
      val node2 = parseNodeObject(nodes(1))
      (node1, node2)
    }.toList

    val edge: List[EdgeObject] = EdgeObjectPreProcess.map {
      case (sourceNode, destinationNode) =>
        EdgeObject(
          sourceNode.nodeId,
          destinationNode.nodeId
        )
    }

    val edgeRdd: RDD[Edge[String]] = sc.parallelize(edge).map { edge =>
      Edge(edge.sourceNodeId, edge.destinationNodeId)
    }
    // Verify edges RDD
//    val collectedEdges = edgeRdd.collect()
//    println("Edges RDD:")
//    collectedEdges.foreach(println)

    (nodesRdd, edgeRdd)
//    sc.stop() // Stop the SparkContext when you're done
  }
}






