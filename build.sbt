name := """playIdss"""

version := "2"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "commons-codec" % "commons-codec" % "1.10",
  "commons-logging" % "commons-logging" % "1.2",
  "org.apache.httpcomponents" % "httpcore" % "4.4.4",
  "org.apache.httpcomponents" % "httpclient" % "4.5.1",
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc4",
  "org.apache.chemistry.opencmis" % "chemistry-opencmis" % "0.13.0",
  "org.apache.chemistry.opencmis" % "chemistry-opencmis-client-api" % "0.13.0",
  "org.apache.chemistry.opencmis" % "chemistry-opencmis-client-bindings" % "0.13.0",
  "org.apache.chemistry.opencmis" % "chemistry-opencmis-client-impl" % "0.13.0",
  "org.apache.chemistry.opencmis" % "chemistry-opencmis-commons-impl" % "0.13.0",
  "org.apache.chemistry.opencmis" % "chemistry-opencmis-commons-api" % "0.13.0",
  "org.apache.commons" % "commons-email" % "1.4",
  "org.avaje.ebeanorm" % "avaje-ebeanorm" % "4.6.2",
  "com.twilio.sdk" % "twilio-java-sdk" % "3.4.5",
  "org.json" % "json" % "20150729",
  "org.apache.jackrabbit" % "jackrabbit-core" % "2.12.1",
  javaJdbc,
  cache,
  javaWs
)

javacOptions ++= Seq("-Xlint:unchecked")
