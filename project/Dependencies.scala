import sbt._

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Jun-2018
  */
object Dependencies {

  object Versions {
    val AkkaHttp: String = "10.1.3"
    val AkkaStream: String = "2.5.11"

    val Sttp: String = "1.2.2"

    val Avro: String = "1.9.0"

    val Log4j: String = "2.8.2"
    val Slf4j: String = "1.7.25"

    val Jackson: String = "2.8.8"
  }

  lazy val Akka: Seq[ModuleID] = Seq(
    "com.typesafe.akka" %% "akka-http" % Versions.AkkaHttp,
    "com.typesafe.akka" %% "akka-stream" % Versions.AkkaStream,
    "com.typesafe.akka" %% "akka-http-caching" % Versions.AkkaHttp
  )

  lazy val Sttp: Seq[ModuleID] = Seq(
    "com.softwaremill.sttp" %% "core" % Versions.Sttp,
    "com.softwaremill.sttp" %% "akka-http-backend" % Versions.Sttp
  )

  lazy val Avro: ModuleID = "com.sksamuel.avro4s" %% "avro4s-core" % Versions.Avro

  lazy val Logging: Seq[ModuleID] = Seq(
    "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
    "org.apache.logging.log4j" % "log4j-api" % Versions.Log4j,
    "org.apache.logging.log4j" % "log4j-api" % Versions.Log4j,
    "org.apache.logging.log4j" % "log4j-slf4j-impl" % Versions.Log4j,
    "org.slf4j" % "slf4j-api" % Versions.Slf4j
  )

  lazy val Jackson: Seq[ModuleID] = Seq(
    "com.fasterxml.jackson.core" % "jackson-core" % Versions.Jackson,
    "com.fasterxml.jackson.core" % "jackson-databind" % Versions.Jackson,
    "com.fasterxml.jackson.core" % "jackson-annotations" % Versions.Jackson,
    "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % Versions.Jackson
  )
}