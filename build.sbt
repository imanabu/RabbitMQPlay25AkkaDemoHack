name := """rabbit-akka"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  "com.rabbitmq" % "amqp-client" % "4.0.2",
  "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

