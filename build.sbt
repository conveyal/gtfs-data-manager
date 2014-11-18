import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.ServerLoader
import NativePackagerKeys._

name := """gtfs-data-manager"""

packageArchetype.java_server

version := "0.1.1"

serverLoading in Rpm := ServerLoader.SystemV

serverLoading in Debian := ServerLoader.SystemV

maintainer in Linux := "Matthew Wigginton Conway <mconway@conveyal.com>"

packageSummary in Linux := "GTFS Data Manager"

packageDescription := "GTFS Data Manager"

rpmRelease := "1"

rpmVendor := "conveyal.com"

rpmUrl := Some("http://github.com/conveyal/gtfs-data-manager")

rpmLicense := Some("MIT")

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
