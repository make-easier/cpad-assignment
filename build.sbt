name := """cpad-assignment"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies ++= Seq(
  guice,
  "mysql" % "mysql-connector-java" % "8.0.19",
  "com.typesafe.play" %% "play-iteratees" % "2.6.1"
)

//resolvers ++= Seq(
//  "sonatype snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
//)