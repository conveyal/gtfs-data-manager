import com.typesafe.sbt.SbtNativePackager._
import com.typesafe.sbt.packager.archetypes.ServerLoader
import NativePackagerKeys._

name := """gtfs-data-manager"""

version := "0.3.3"

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
  "com.google.guava" % "guava" % "18.0",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.9.3",
  "commons-io" % "commons-io" % "2.4",
  "net.sourceforge.javacsv" % "javacsv" % "2.0",
  "com.conveyal" % "gtfs-validator-json" % "0.0.1-SNAPSHOT" exclude("org.slf4j", "slf4j-simple")
)

resolvers += "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository"
resolvers += "Conveyal Maven Repository" at "http://maven.conveyal.com"
resolvers += "Geotoolkit Maven Repository" at "http://maven.geotoolkit.org"
resolvers += "OSGeo" at "http://download.osgeo.org/webdav/geotools/"
