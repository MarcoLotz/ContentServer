organization := "com.marcolotz"

name := "ContentServer"

version := "0.1-SNAPSHOT"

scalaVersion := "2.12.0"

Seq(webSettings: _*)

libraryDependencies ++= Seq(
  "com.github.scopt" %% "scopt" % "3.5.0",
  "org.json4s" %% "json4s-jackson" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "org.scalatra" %% "scalatra" % "2.5.1",
  "org.scalatra" %% "scalatra-auth" % "2.5.1",
  "org.scalatra" %% "scalatra-scalate" % "2.5.1",
  "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910" % "container,compile",
  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.scalactic" %% "scalactic" % "3.0.1",
  "org.scalatest" %% "scalatest" % "3.0.1" % "test",
  "junit" % "junit" % "4.12" % "test"
)
