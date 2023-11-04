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

    val graph2 = NetGraph.load(args(1))
    val getOriginalNodes = graph2.head.sm.nodes().toString
    val processed_nodes = getOriginalNodes.drop(1).dropRight(1).split(", ")

    val (nodesRdd, edgeRdd) = PreProcessor.preprocessGraph(args(0), sc)
    logger.info("Rdds Created")
    // Now you have access to nodesRdd and edgeRdd in your main method
//    println("Nodes RDD:")
//    nodesRdd.collect().foreach(println)

//    println("Edges RDD:")
//    edgeRdd.collect().foreach(println)

    val graph = Graph(nodesRdd, edgeRdd)
    val maxSteps = 3

    // Get a list of all nodes from nodesRdd (assuming nodesRdd is of type RDD[(VertexId, NodeObject)])
    val allNodes: List[VertexId] = nodesRdd.map(_._1).collect().toList

    // Perform random walks for each start node and collect the results in a list of lists
    val randomPaths: List[List[VertexId]] = randomWalks(graph, allNodes, maxSteps)

    if (randomPaths != null)
      logger.info("random paths generated")
    else
      logger.error("Error generating Random Walks please check Input file!!")


    val originalNodes: List[NodeObject] = processed_nodes.map(parseNodeObject).toList


    val comparisonResultsBuffer: ListBuffer[String] = ListBuffer()
    // Iterate through each random path
    randomPaths.foreach { path =>
      // Iterate through each node in the random path
      path.foreach { perturbedNodeId =>
        // Lookup the NodeObject for the current randomNodeId
        val perturbedNodeObject: NodeObject = nodesRdd.lookup(perturbedNodeId).head
        // Create a list to store comparison results for the current PertubedNode
        val nodeComparisonResults: List[(Int, Double, Boolean)] = originalNodes.map { originalNode =>
          val jaccardIndex = calculateJaccardIndex(originalNode, perturbedNodeObject)
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
    comparisonResults.foreach(logger.info)
    calculateAlgorithmStats(comparisonResults,args(2),args(3),randomPaths)



  }

}
