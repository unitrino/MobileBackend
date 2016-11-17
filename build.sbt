name := "untitled3"

version := "1.0"

scalaVersion := "2.11.8"

mainClass := Some("Main")

libraryDependencies ++= Seq (
  "com.typesafe.akka" %% "akka-actor" % "2.4.10",
  "com.typesafe.akka" %% "akka-stream" % "2.4.10",
  "net.ruippeixotog" %% "scala-scraper" % "1.0.0",
  "com.typesafe.akka" %% "akka-http-experimental" % "2.4.11",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.typesafe.slick" %% "slick" % "3.1.1",
  "com.github.tototoshi" %% "slick-joda-mapper" % "2.2.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7",
  "com.typesafe.akka" %% "akka-http-spray-json-experimental" % "2.4.4",
  "joda-time" % "joda-time" % "2.7",
  "org.joda" % "joda-convert" % "1.7",
  "com.zaxxer" % "HikariCP" % "2.5.1"
)