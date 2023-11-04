import NetGraphAlgebraDefs.NetGraph
import org.apache.spark.SparkContext
import org.apache.spark.graphx.{Edge, VertexId}
import org.apache.spark.rdd.RDD
import org.slf4j.LoggerFactory


object PreProcessor {
  private val logger = LoggerFactory.getLogger(getClass)

  def preprocessGraph(outputDirectory: String, sc: SparkContext): (RDD[(VertexId, NodeObject)], RDD[Edge[String]]) = {

    val graph2 = NetGraph.load(outputDirectory)

    val edges = graph2.head.sm.edges().toString

    val nodes = graph2.head.sm.nodes().toString

    val processed_nodes = nodes.drop(1).dropRight(1).split(", ")
    val processed_edges = edges.dropRight(2).drop(2).split(">, <").map(_.trim)


    val NodeObjectPreProcess = processed_nodes.map(parseNodeObject).toList
    val nodesRdd: RDD[(VertexId, NodeObject)] = sc.parallelize(NodeObjectPreProcess).map {
      node => (node.nodeId, node)
    }

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

    logger.info("Preprocessing completed.")
    (nodesRdd, edgeRdd)
  }

  def parseNodeObject(input: String): NodeObject = {
    val pattern = "NodeObject\\((\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+\\.\\d+),(.+)\\)".r
    input match {
      case pattern(id, a, b, c, d, e, f, g, h, i) =>
        NodeObject(id.toInt, a.toInt, b.toInt, c.toInt, d.toInt, e.toInt, f.toInt, g.toInt, h.toDouble, i.toBoolean)
      case _ =>
        val errorMsg = "cannot parse please check input format!"
        logger.error(errorMsg)
        throw new IllegalArgumentException(errorMsg)
    }
  }
}






