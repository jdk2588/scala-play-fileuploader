name := """play-reactive-mongo-db"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

routesGenerator := InjectedRoutesGenerator

ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }

libraryDependencies ++= Seq(
  "org.reactivemongo" %% "play2-reactivemongo" % "0.11.12"
)

libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.0" % "test"
)

libraryDependencies += "com.typesafe.akka" %% "akka-stream" % "2.4.6"

libraryDependencies += "com.github.seratch" %% "awscala" % "0.6.+"

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"


scalacOptions in ThisBuild ++= Seq("-feature", "-language:postfixOps")

fork in run := true