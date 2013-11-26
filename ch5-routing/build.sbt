name := "ch5-routing"

version := "1.0"

scalaVersion := "2.10.1"

libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-server" % "9.0.0.v20130308",
  "org.eclipse.jetty" % "jetty-webapp" % "9.0.0.v20130308",
  "com.typesafe.akka" %% "akka-actor" % "2.2.3"
)
