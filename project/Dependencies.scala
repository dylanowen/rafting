import sbt._

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Jun-2018
  */
object Dependencies {

  object Versions {
    val Akka: String = "10.1.1"
    val AkkaStream: String = "2.5.11"

    val Sttp: String = "1.2.2"

    val Log4j: String = "2.8.2"
    val Slf4j: String = "1.7.25"

    val Jackson: String = "2.8.8"
  }

  lazy val Akka: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-http" % Versions.Akka,
    "com.typesafe.akka" %% "akka-stream" % Versions.AkkaStream
    //"com.typesafe.akka" %% "akka-http2-support" % Versions.Akka
  )

  lazy val Sttp: Seq[ModuleID] = Seq(
    "com.softwaremill.sttp" %% "core" % Versions.Sttp,
    "com.softwaremill.sttp" %% "akka-http-backend" % Versions.Sttp
  )

  lazy val ScalaLogging: ModuleID = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"
  lazy val Log4jApi: ModuleID = "org.apache.logging.log4j" % "log4j-api" % Versions.Log4j
  lazy val Log4jCore: ModuleID = "org.apache.logging.log4j" % "log4j-api" % Versions.Log4j
  lazy val Log4jSlf4j: ModuleID = "org.apache.logging.log4j" % "log4j-slf4j-impl" % Versions.Log4j
  lazy val Slf4j: ModuleID = "org.slf4j" % "slf4j-api" % Versions.Slf4j

  // Required for Logging Properties Parsing
  lazy val JacksonCore: ModuleID = "com.fasterxml.jackson.core" % "jackson-core" % Versions.Jackson
  lazy val JacksonDatabind: ModuleID = "com.fasterxml.jackson.core" % "jackson-databind" % Versions.Jackson
  lazy val JacksonAnnotations: ModuleID = "com.fasterxml.jackson.core" % "jackson-annotations" % Versions.Jackson
  lazy val JacksonDataformat: ModuleID = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % Versions.Jackson
}