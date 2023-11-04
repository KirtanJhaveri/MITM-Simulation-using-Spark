// Main.scala
import FinalStatsCalc.calculateAlgorithmStats
import org.slf4j.LoggerFactory
//import org.apache.log4j.spi.LoggerFactory
import NetGraphAlgebraDefs.NetGraph
import PreProcessor.parseNodeObject
import RandomWalkGenerator.randomWalks
import SimScoreCalc.calculateJaccardIndex
import org.apache.spark.graphx.{Graph, VertexId}
import org.apache.spark.{SparkConf, SparkContext}
//import org.slf4j.LoggerFactory
import scala.collection.mutable.ListBuffer


object Main {
  private val sparkConf: SparkConf = new SparkConf().setAppName("Man-In-The-Middle-Attack-Simulation").setMaster("local[*]")
  val sc = new SparkContext(sparkConf)
  private val logger = LoggerFactory.getLogger(getClass)
//  Logger.getLogger("org.apache.spark").setLevel(Level.DEBUG)

  def main(args: Array[String]): Unit = {
//    val outputDirectory = args(0)
    val graph2 = NetGraph.load(args(1))
    val getOriginalNodes = graph2.head.sm.nodes().toString
    val processed_nodes = getOriginalNodes.drop(1).dropRight(1).split(", ")

    val (nodesRdd, edgeRdd) = PreProcessor.preprocessGraph(args(0), sc)
    logger.info("Rdds Created")
    // Now you have access to nodesRdd and edgeRdd in your main method
    println("Nodes RDD:")
    nodesRdd.collect().foreach(println)

    println("Edges RDD:")
    edgeRdd.collect().foreach(println)

    val graph = Graph(nodesRdd, edgeRdd)
    val maxSteps = 3
    //    val allNodes: List[VertexId] = nodesRdd.map(_._1).collect().toList
    //    val randomPaths: List[List[VertexId]] = randomWalk(graph, allNodes, maxSteps)
    //    println(randomPaths)

    // Get a list of all nodes from nodesRdd (assuming nodesRdd is of type RDD[(VertexId, NodeObject)])
    val allNodes: List[VertexId] = nodesRdd.map(_._1).collect().toList

    // Perform random walks for each start node and collect the results in a list of lists
    val randomPaths: List[List[VertexId]] = randomWalks(graph, allNodes, maxSteps)
//    println("randomPaths")
//    println(randomPaths)
    logger.info("random paths generated")
    // Print the results (optional)
    randomPaths.foreach(println)

    val originalNodes: List[NodeObject] = processed_nodes.map(parseNodeObject).toList
//    println(originalNodes(1))

    val comparisonResultsBuffer: ListBuffer[String] = ListBuffer()
    // Iterate through each random path
    randomPaths.foreach { path =>
      // Iterate through each node in the random path
      path.foreach { perturbedNodeId =>
        // Lookup the NodeObject for the current randomNodeId
        val randomNodeObject: NodeObject = nodesRdd.lookup(perturbedNodeId).head
        // Create a list to store comparison results for the current randomNodeId
        val nodeComparisonResults: List[(Int, Double, Boolean)] = originalNodes.map { originalNode =>
          val jaccardIndex = calculateJaccardIndex(originalNode, randomNodeObject)
          (originalNode.nodeId, jaccardIndex, originalNode.field9)
        }

        // Sort the comparison results list in descending order of the Jaccard index
        val highestJaccardNode = nodeComparisonResults.maxBy(_._2)
        val formattedResult = s"$perturbedNodeId : ${highestJaccardNode._1}, ${highestJaccardNode._2}, ${highestJaccardNode._3}"

        // Add the formatted string to the ListBuffer
        comparisonResultsBuffer += formattedResult
      }
    }

    // Convert the ListBuffer to an immutable List
    val comparisonResults: List[String] = comparisonResultsBuffer.toList
//    comparisonResults.foreach(println)
    comparisonResults.foreach(logger.info)
    calculateAlgorithmStats(comparisonResults,args(2),args(3),randomPaths)

////    import java.io.PrintWriter
////
////    // Your modified code for counting successful and failed attacks
////    var successfulAttack = 0
////    var failedAttack = 0
////    var honeyPot = 0
////    var noAttackPerformed = 0
////
////    comparisonResults.foreach { result =>
////      val parts = result.split("[:,]").map(_.trim)
////      if (parts.length == 4) {
////        val randomNodeId = parts(0).toInt
////        val originalNodeId = parts(1).toInt
////        val jaccardIndex = parts(2).toDouble
////        val booleanValue = parts(3).toBoolean
////
////        if (jaccardIndex > 0.8) {
////          if (booleanValue) {
////            if(randomNodeId == originalNodeId )
////            successfulAttack += 1
////            else
////              failedAttack += 1
////          }
////          else
////            noAttackPerformed += 1
////        } else {
////          honeyPot += 1
////        }
////      }
////    }
////

  }

}







//import NetGraphAlgebraDefs.NetGraph
//import org.apache.spark.graphx.{Edge, VertexId}
//import org.apache.spark.rdd.RDD
//import org.slf4j.LoggerFactory
////import NetGraphAlgebraDefs.NetModelAlgebra.{actionType, outputDirectory}
////import scala.concurrent.duration.*
//
////import org.apache.spark.SparkConf
////import org.apache.spark.SparkContext
////import org.apache.spark.graphx._
////import org.apache.spark.sql.SparkSession
//import org.apache.spark.{SparkConf, SparkContext}
//
//case class EdgeObject(sourceNodeId: Int, destinationNodeId: Int)
////case class vertexObject(vId: Int, nodeObject: NodeObject)
//case class NodeObject(nodeId: Int, field1: Int, field2: Int, field3: Int, field4: Int, field5: Int, field6: Int, field7: Int, field8: Double, field9: Boolean)
//
//object Main {
//  private val logger = LoggerFactory.getLogger(getClass)
//
//  private def parseNodeObject(input: String): NodeObject = {
////    println("parseNodeObject")
////    println(input)
//    val pattern = "NodeObject\\((\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+),(\\d+\\.\\d+),(.+)\\)".r
//    input match {
//      case pattern(id, a, b, c, d, e, f, g, h, i) =>
//        NodeObject(id.toInt, a.toInt, b.toInt, c.toInt, d.toInt, e.toInt, f.toInt, g.toInt, h.toDouble, i.toBoolean)
//      case _ => throw new IllegalArgumentException("cannot parse please check input format!")
//    }
//  }
//  def main(args: Array[String]): Unit = {
//    val sparkConf = new SparkConf().setAppName("NodeEdgeParser").setMaster("local[*]")
//    val sc = new SparkContext(sparkConf)
//
//    val outputDirectory = args(0)
//    val graph2 = NetGraph.load("10_nodes.ngs",outputDirectory)
////    println(graph2)
//    val edges = graph2.head.sm.edges().toString
////    println(edges)
//    val nodes = graph2.head.sm.nodes().toString
////    println(nodes)
//    val processed_nodes = nodes.drop(1).dropRight(1).split(", ")
////    processed_nodes.foreach(println)
//    val NodeObjectPreProcess = processed_nodes.map(parseNodeObject).toList
////    println(newNodeObjectPreProcess.mkString("Array(", ", ", ")"))
//    val nodesRdd: RDD[(VertexId,NodeObject)]= sc.parallelize(NodeObjectPreProcess).map{
//      node=>(node.nodeId,node)
//    }
//    val collectedNodes = nodesRdd.collect()
//    println("Nodes RDD:")
//    collectedNodes.foreach(println)
//
//    val processed_edges = edges.dropRight(2).drop(2).split(">, <").map(_.trim)
////    processed_edges.foreach(println)
//
//    val EdgeObjectPreProcess: List[(NodeObject, NodeObject)] = processed_edges.map { line =>
//      val nodes = line.split(" -> ")
//      val node1 = parseNodeObject(nodes(0))
//      val node2 = parseNodeObject(nodes(1))
//      (node1, node2)
//    }.toList
//
//    val edge: List[EdgeObject] = EdgeObjectPreProcess.map {
//      case (sourceNode, destinationNode) =>
//        EdgeObject(
//          sourceNode.nodeId,
//          destinationNode.nodeId
//        )
//    }
//
//    val edgeRdd: RDD[Edge[String]] = sc.parallelize(edge).map { edge =>
//      Edge(edge.sourceNodeId, edge.destinationNodeId)
//    }
//    // Verify edges RDD
//    val collectedEdges = edgeRdd.collect()
//    println("Edges RDD:")
//    collectedEdges.foreach(println)
//
//    sc.stop() // Stop the SparkContext when you're done
//  }
//}
//
//
//
//
//
//
