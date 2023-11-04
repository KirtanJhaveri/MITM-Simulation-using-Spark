ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.12"
val guavaVersion = "31.1-jre"
lazy val root = (project in file("."))
  .settings(
    name := "MITM-Simulation-using-Spark"
  )

scalacOptions += "-Ytasty-reader"

//libraryDependencies ++= Seq(
//  "org.apache.spark" %% "spark-core" % "3.4.1" exclude("com.google.guava", "guava") exclude("org.slf4j", "slf4j-log4j12"),
//  "org.apache.spark" %% "spark-graphx" % "3.4.1" exclude("com.google.guava", "guava") exclude("org.slf4j", "slf4j-log4j12"),
//  "org.apache.spark" %% "spark-sql" % "3.4.1" exclude("com.google.guava", "guava") exclude("org.slf4j", "slf4j-log4j12"),
//  "com.google.guava" % "guava" % guavaVersion,
//).map(_.exclude("org.slf4j","*"))
libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "3.4.1" exclude("com.google.guava", "guava") ,
  "org.apache.spark" %% "spark-graphx" % "3.4.1" exclude("com.google.guava", "guava") ,
  "org.apache.spark" %% "spark-sql" % "3.4.1" exclude("com.google.guava", "guava") ,
  "com.google.guava" % "guava" % guavaVersion,
)
libraryDependencies += "org.yaml" % "snakeyaml" % "1.29"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.9" % "test"
libraryDependencies += "org.scalaj" %% "scalaj-http" % "2.4.2"



exportJars := true
//libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.4.7"
//libraryDependencies += "org.slf4j" % "slf4j-api" % "2.0.5"

compileOrder := CompileOrder.JavaThenScala
test / fork := true
run / fork := true
run / javaOptions ++= Seq(
  "-Xms8G",
  "-Xmx100G",
  "-XX:+UseG1GC"
)

Compile / mainClass := Some("Main")
run / mainClass := Some("Main")

val jarName = "mitm1.jar"
assembly / assemblyJarName := jarName

// Merging strategies
ThisBuild / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}