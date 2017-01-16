name := """drivers"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  evolutions,
  cache,
  ws,
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test,
  "org.postgresql" % "postgresql" % "9.4-1201-jdbc41",
  "com.typesafe.slick" %% "slick" % "3.1.0",
  "com.typesafe.play" %% "play-slick" % "2.0.2",
  "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2",
  "com.vividsolutions" % "jts" % "1.13",
  "com.github.tminglei" %% "slick-pg" % "0.14.4",
  "com.github.tminglei" %% "slick-pg_date2" % "0.14.4",
  "com.github.tminglei" %% "slick-pg_jts" % "0.14.4",
  "com.github.tminglei" %% "slick-pg_play-json" % "0.14.4"
)

