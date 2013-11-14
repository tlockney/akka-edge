name := "ch10-akka-cluster"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-cluster" % "2.2.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3",
  "com.typesafe.akka" %% "akka-multi-node-testkit" % "2.2.3",
  "org.scalatest" %% "scalatest" % "2.0.M7" % "compile,test"
)

