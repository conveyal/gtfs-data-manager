name := """gtfs-data-manager"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  javaJdbc,
  javaEbean,
  cache,
  javaWs,
  "org.mapdb" % "mapdb" % "1.0.6",
  "org.julienrf" %% "play-jsmessages" % "1.6.2",
  "com.google.guava" % "guava" % "18.0"
)
