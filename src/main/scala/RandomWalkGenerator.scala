import org.apache.spark.graphx.{Graph, VertexId}

import scala.annotation.tailrec
import scala.util.Random
import org.slf4j.{Logger, LoggerFactory}

object RandomWalkGenerator {
  private val logger: Logger = LoggerFactory.getLogger(getClass)
  def randomWalks(graph: Graph[NodeObject, String], startNodes: List[VertexId], maxSteps: Int): List[List[VertexId]] = {
    def randomWalkForNode(startNode: VertexId, visited: Set[VertexId]): List[VertexId] = {
      def randomNeighbor(node: VertexId, visited: Set[VertexId]): Option[VertexId] = {
        val neighbors = graph.edges.filter(edge => edge.srcId == node && !visited.contains(edge.dstId)).map(_.dstId).collect()
        if (neighbors.isEmpty) None else Some(neighbors(Random.nextInt(neighbors.length)))
      }

      @tailrec
      def walk(currentNode: VertexId, steps: Int, path: List[VertexId], visited: Set[VertexId]): List[VertexId] = {
        if (steps >= maxSteps || visited.size == graph.numVertices) path.reverse
        else randomNeighbor(currentNode, visited) match {
          case Some(neighbor) => walk(neighbor, steps + 1, neighbor :: path, visited)
          case None => path.reverse
        }
      }

      walk(startNode, 0, List(startNode), visited)
    }

    // Set to keep track of visited nodes across all random walks
    var visitedNodes = Set.empty[VertexId]

    // List to store the results of random walks
    var randomPaths: List[List[VertexId]] = List()

    // Perform random walks for each start node
    startNodes.foreach { startNode =>
      if (visitedNodes.contains(startNode)) {
        // val path1 = List.empty // Creating an empty list of appropriate type
        logger.info(s"Skipping random walk for already visited node: $startNode")
//        randomPaths = randomPaths // Adding path1 to randomPaths
      } else {
        val path = randomWalkForNode(startNode, visitedNodes)
        visitedNodes ++= path
        // println("visitedNodes:")
        // println(visitedNodes)
//        logger.info(s"Random walk path for node $startNode: ${path.mkString(" -> ")}")
        randomPaths = randomPaths :+ path
      }
    }
    randomPaths

  }

}

//  def randomWalk(graph: Graph[NodeObject, String], startNodes: List[VertexId], maxSteps: Int): List[List[VertexId]] = {
//    def randomNeighbor(node: VertexId): Option[VertexId] = {
//      val neighbors = graph.edges.filter(_.srcId == node).map(_.dstId).collect()
//      if (neighbors.isEmpty) None else Some(neighbors(Random.nextInt(neighbors.length)))
//    }
//
//    def walk(currentNode: VertexId, steps: Int, path: List[VertexId]): List[VertexId] = {
//      if (steps >= maxSteps) path.reverse
//      else randomNeighbor(currentNode) match {
//        case Some(neighbor) => walk(neighbor, steps + 1, neighbor :: path)
//        case None => path.reverse
//      }
//    }
//
//    def randomWalkForNode(startNode: VertexId): List[VertexId] = {
//      walk(startNode, 0, List(startNode))
//    }
//
//    startNodes.map(randomWalkForNode)
//  }

//    private def generateRandomWalks(edges: RDD[Edge[String]], maxDepth: Int): Array[List[VertexId]] = {
//
//      var paths: Array[List[VertexId]] = Array.empty
//      val visitedNodes: Set[VertexId] = Set.empty
//
//    def dfs(nodeId: VertexId, path: List[VertexId], depth: Int): Unit = {
//      if (depth <= maxDepth && !visitedNodes.contains(nodeId)) {
//        // Visit the node
//        val newPath = nodeId :: path
//        println("new-path")
//        newPath.foreach(println)
//
//        // Add the path to the array
//        paths :+= newPath.reverse
//        visitedNodes += nodeId
//
//        // Explore neighbors
//        edges.filter(e => e.srcId == nodeId).collect().foreach { edge =>
//          dfs(edge.dstId, newPath, depth + 1)
//        }
//      }
//    }
//
//    val allNodeIds = edges.flatMap(e => Array(e.srcId, e.dstId)).distinct().collect()
//    val random = new Random()
//
//    while (visitedNodes.size < allNodeIds.length) {
//      val randomNodeId = allNodeIds(random.nextInt(allNodeIds.length))
//      dfs(randomNodeId, List.empty, 0)
//    }
//    paths
//  }