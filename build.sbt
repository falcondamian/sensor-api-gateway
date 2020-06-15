val finchVersion = "0.32.1"
val circeVersion = "0.10.1"
val scalatestVersion = "3.0.5"
val scalaLoggingVersion = "3.9.2"
val logbackVersion = "1.2.3"
val catbirdVersion = "20.3.0"
val doobieVersion = "0.8.8"

enablePlugins(JavaAppPackaging)

lazy val root = (project in file("."))
  .settings(
    organization := "com.falcon",
    name := "sensor-api-gateway",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.12.7",
    libraryDependencies ++= Seq(
      "com.github.finagle" %% "finchx-core"  % finchVersion,
      "com.github.finagle" %% "finchx-circe"  % finchVersion,
      "io.catbird" %% "catbird-effect" % catbirdVersion,
      "io.circe" %% "circe-generic" % circeVersion,
      "ch.qos.logback" % "logback-classic" % logbackVersion,
      "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
      "org.tpolecat" %% "doobie-core"  % doobieVersion,
      "org.tpolecat" %% "doobie-postgres"  % doobieVersion,
      "org.tpolecat" %% "doobie-hikari"  % doobieVersion,
      "org.scalatest"      %% "scalatest"    % scalatestVersion % "test"
    )
  )