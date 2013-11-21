import play.Project._

name := "play_with_akka_cluster"

version := "1.0"

scalaVersion := "2.10.2"

libraryDependencies ++= Seq(
  "ch10-akka-cluster" %% "ch10-akka-cluster" % "1.0",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3",
  "com.typesafe.akka" %% "akka-cluster" % "2.2.3",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.3",
  "com.typesafe.akka" %% "akka-multi-node-testkit" % "2.2.3",
  "org.scalatest" %% "scalatest" % "2.0.M7" % "compile,test"
)

playScalaSettings

