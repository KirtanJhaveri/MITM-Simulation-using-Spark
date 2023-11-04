import org.apache.spark.graphx.VertexId

import java.io.PrintWriter
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml

import java.nio.file.{Files, Paths}
import scala.io.Source
import scala.collection.JavaConverters._

object FinalStatsCalc{
  private val logger = LoggerFactory.getLogger(getClass)

  import scalaj.http.Http

  // ...

  private def readYamlDataFromHttp(yamlPath: String): String = {
    try {
      val response = Http(yamlPath).asString
      if (response.isSuccess) {
        response.body.replaceAll("\t", "  ")
      } else {
        // Handle HTTP error, e.g., log the error
        println(s"Failed to fetch YAML from $yamlPath. HTTP Status: ${response.code}")
        ""
      }
    } catch {
      case e: Exception =>
        // Handle other exceptions, e.g., log the error
        e.printStackTrace()
        ""
    }
  }
  def calculateAlgorithmStats(comparisonResults: List[String], yamlPath: String, outputFilePath : String, randomPaths: List[List[VertexId]]): Unit = {


//    val yamlPath = "http://example.com/path/to/your/yaml.yaml"
    val yamlData: String = if (yamlPath.startsWith("http")) {
      readYamlDataFromHttp(yamlPath)
    } else {
      new String(Files.readAllBytes(Paths.get(yamlPath))).replaceAll("\t", "  ")
    }

    // Parse the YAML string
//    val yamlData = new String(Files.readAllBytes(Paths.get(yamlPath))).replaceAll("\t", "  ")
    val yaml = new Yaml()
    val yamlMap = yaml.load(yamlData).asInstanceOf[java.util.Map[String, Any]]

    // Extract Nodes section
    val nodesSection = yamlMap.get("Nodes").asInstanceOf[java.util.Map[String, Any]]
    val addedNodes: Set[Int] = Option(yamlMap.get("Added").asInstanceOf[java.util.Map[Int, Int]])
      .map(_.keySet().asScala.toSet)
      .getOrElse(Set.empty[Int])

    val modifiedNodes = Option(nodesSection.get("Modified").asInstanceOf[java.util.List[Int]])
      .map(_.asScala.toList)
      .getOrElse(List.empty[Int])

    val removedNodes = Option(nodesSection.get("Removed").asInstanceOf[java.util.List[Int]])
      .map(_.asScala.toList)
      .getOrElse(List.empty[Int])

    val (successfulAttack, failedAttack, noAttackPerformed) = comparisonResults.foldLeft((0, 0, 0)) {
      case ((success, failed, noAttack), result) =>
        val parts = result.split("[:,]").map(_.trim)
        if (parts.length == 4) {
          val randomNodeId = parts(0).toInt
          val originalNodeId = parts(1).toInt
          val jaccardIndex = parts(2).toDouble
          val booleanValue = parts(3).toBoolean

          val (newSuccess, newFailed, newNoAttack) =
            if (jaccardIndex > 0.0) {
              if (booleanValue) {
                if (addedNodes.contains(randomNodeId) || modifiedNodes.contains(originalNodeId))
                  (success, failed + 1, noAttack)
                else
                  (success + 1, failed, noAttack)
              } else
                (success, failed, noAttack + 1)
            } else
              (success, failed, noAttack + 1)

          (newSuccess, newFailed, newNoAttack)
        } else
          (success, failed, noAttack + 1)
    }

    val innerListSizes: List[Int] = randomPaths.map(_.size)

    // Calculate min, max, mean, and median
    val minSize: Int = innerListSizes.min
    val maxSize: Int = innerListSizes.max
    val meanSize: Double = innerListSizes.sum.toDouble / innerListSizes.size
    val sortedSizes: List[Int] = innerListSizes.sorted
    val medianSize: Double =
      if (sortedSizes.size % 2 == 0) {
        val middle1 = sortedSizes((sortedSizes.size - 1) / 2)
        val middle2 = sortedSizes(sortedSizes.size / 2)
        (middle1 + middle2) / 2.0
      } else {
        sortedSizes(sortedSizes.size / 2)
      }

    logger.info(s"Min nodes in a random walk: $minSize")
    logger.info(s"Max nodes in a random walk: $maxSize")
    logger.info(s"Mean number of nodes in a random walk: $meanSize")
    logger.info(s"Median number of nodes in a random walk: $medianSize")

    logger.info(s"Successful Attacks: $successfulAttack")
    logger.info(s"Failed Attacks: $failedAttack")
    //    logger.info(s"Invaluable Data Nodes: $noAttackPerformed")
    // Print the results

    println(s"Min nodes in a random walk: $minSize")
    println(s"Max nodes in a random walk: $maxSize")
    println(s"Mean number of nodes in a random walk: $meanSize")
    println(s"Median number of nodes in a random walk: $medianSize")
    println(s"Successful Attacks: $successfulAttack")
    println(s"Failed Attacks: $failedAttack")
    val writer = new PrintWriter(outputFilePath)
    writer.println(s"Successful Attacks: $successfulAttack")
    writer.println(s"Failed Attacks: $failedAttack")
    writer.println(s"Min nodes in a random walk: $minSize")
    writer.println(s"Max nodes in a random walk: $maxSize")
    writer.println(s"Mean number of nodes in a random walk: $meanSize")
    writer.println(s"Median number of nodes in a random walk: $medianSize")
    writer.println(s"Successful Attacks: $successfulAttack")
    writer.println(s"Failed Attacks: $failedAttack")
//    writer.println(s"Invaluable Data Nodes: $noAttackPerformed")
    writer.close()

//    println(s"Invaluable Data Nodes: $noAttackPerformed")
  }
}


//    comparisonResults.foreach { result =>
//      val parts = result.split("[:,]").map(_.trim)
//      if (parts.length == 4) {
//        val randomNodeId = parts(0).toInt
//        val originalNodeId = parts(1).toInt
//        val jaccardIndex = parts(2).toDouble
//        val booleanValue = parts(3).toBoolean
//
//        if (jaccardIndex > 0.8) {
//          if (booleanValue) {
//            if(addedNodes.contains(randomNodeId) || modifiedNodes.contains(originalNodeId))
//              failedAttack += 1
//            else
//              successfulAttack += 1
//          }
//          else
//            noAttackPerformed += 1
//        } else {
//          noAttackPerformed += 1
//        }
//      }
//    }
