name := """connect4-web"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

// resolvers += "lenny-htwg" at "http://lenny2.in.htwg-konstanz.de:8081/artifactory/libs-snapshot-local"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test,
  "org.scala-lang" % "scala-swing" % "2.11+"
)
//   "com.github.connect4plus" % "connect4plus_2.11" % "1.0-SNAPSHOT"
resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator
