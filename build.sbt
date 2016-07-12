name := """jedzonko"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  cache,
  filters,
  ws,
  specs2 % Test
)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "1.1.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "1.1.1",
  "org.postgresql" % "postgresql" % "9.3-1102-jdbc41",
  "com.typesafe.slick" %% "slick-codegen" % "3.1.1",
  "net.codingwell" %% "scala-guice" % "4.0.0",
  "com.mohiva" %% "play-silhouette" % "3.0.4",
  "com.typesafe.play" %% "play-mailer" % "3.0.1",
  "org.webjars" %% "webjars-play" % "2.4.0-1",
  "net.ceedubs" %% "ficus" % "1.1.2",
  "com.adrianhurt" %% "play-bootstrap3" % "0.4.4-P24"
)

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Atlassian Releases" at "https://maven.atlassian.com/public/"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
