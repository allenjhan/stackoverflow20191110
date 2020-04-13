name := "stackoverflow20191110"

version := "0.1"

scalaVersion := "2.12.1"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % "0.12.2")

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.4.16"