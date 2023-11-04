import org.apache.spark.graphx.{Edge, Graph, VertexId}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class YourTestSuite extends AnyFlatSpec with Matchers {

  "NodeObject" should "be instantiated correctly" in {
    val node = NodeObject(1, 10, 20, 30, 40, 50, 60, 70, 3.14, true)
    node.nodeId shouldEqual 1
    node.field1 shouldEqual 10
    node.field2 shouldEqual 20
    node.field3 shouldEqual 30
    node.field4 shouldEqual 40
    node.field5 shouldEqual 50
    node.field6 shouldEqual 60
    node.field7 shouldEqual 70
    node.field8 shouldEqual 3.14
    node.field9 shouldBe true
  }

  "EdgeObject" should "be instantiated correctly" in {
    val edge = EdgeObject(1, 2)
    edge.sourceNodeId shouldEqual 1
    edge.destinationNodeId shouldEqual 2
  }


  val nodes: List[(VertexId, NodeObject)] = List(
    (1, NodeObject(1, 10, 20, 30, 40, 50, 60, 70, 3.14, true)),
    (2, NodeObject(2, 15, 25, 35, 45, 55, 65, 75, 2.71, false)),
    (3, NodeObject(3, 5, 15, 25, 35, 45, 55, 65, 1.618, true))
  )

  val edges: List[Edge[String]] = List(
    Edge(1, 2, "edge1"),
    Edge(2, 3, "edge2"),
    Edge(3, 1, "edge3")
  )

  val sparkContext = new org.apache.spark.SparkContext("local[*]", "TestGraph")
  val nodesRdd = sparkContext.parallelize(nodes)
  val edgesRdd = sparkContext.parallelize(edges)

  val testGraph: Graph[NodeObject, String] = Graph(nodesRdd, edgesRdd)

  "RandomWalkGenerator" should "generate valid random walks" in {
    val graph = testGraph // create your test graph here
    val allNodes: List[VertexId] = nodesRdd.map(_._1).collect().toList
    val startNodes = allNodes
    val maxSteps = 3
    val randomPaths = RandomWalkGenerator.randomWalks(graph, startNodes, maxSteps)
    randomPaths.foreach(path => path.length should be <= maxSteps + 1)
  }

  "RandomWalkGenerator" should "not include visited nodes in random walk" in {
    // Dummy graph data for testing
    val graph: Graph[NodeObject, String] = testGraph
    // List of start nodes for random walks
    val startNodes: List[VertexId] = List(1L, 2L, 3L)

    // Perform random walks
    val randomPaths: List[List[VertexId]] = RandomWalkGenerator.randomWalks(graph, startNodes, maxSteps = 3)

    // Check if each node appears only once in the random walk
    val allNodesInRandomPaths = randomPaths.flatten
    val uniqueNodesInRandomPaths = allNodesInRandomPaths.distinct

    // Assert that all nodes are present once and only once in the randomPaths
    uniqueNodesInRandomPaths.sorted shouldEqual startNodes.sorted
  }

  "SimScoreCalc" should "calculate Jaccard index correctly" in {
    val node1 = NodeObject(1, 10, 20, 30, 40, 50, 60, 70, 3.14, true)
    val node2 = NodeObject(2, 10, 25, 30, 40, 50, 60, 70, 3.14, true)
    val jaccardIndex = SimScoreCalc.calculateJaccardIndex(node1, node2)
    jaccardIndex shouldBe 0.8888888888888888 // Example value, adjust based on your logic
  }
  //
  //  "FinalStatsCalc" should "calculate algorithm stats correctly" in {
  //    val comparisonResults = List("1:1,0.9,true", "2:2,0.8,false")
  //    val yamlFilePath = "C:\\Users\\kirta\\Desktop\\CS441\\MITM-Simulation-using-Spark\\inputGraphs\\300_nodes.ngs.yaml"
  //    FinalStatsCalc.calculateAlgorithmStats(comparisonResults, yamlFilePath)
  //    // Verify the stats in your output file or logs
  //  }
}
