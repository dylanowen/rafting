enablePlugins(JavaAppPackaging)
enablePlugins(DockerPlugin)
enablePlugins(AshScriptPlugin)

name := "rafting"

organization in ThisBuild := "com.dylowen"
version in ThisBuild := "0.1"
scalaVersion in ThisBuild := "2.12.6"
scalacOptions in ThisBuild ++= Seq(
  "-feature",
  "-deprecation"
)

libraryDependencies ++= Dependencies.Akka
libraryDependencies ++= Dependencies.Sttp

mainClass in Compile := Some("com.dylowen.rafting.RaftApp")

dockerBaseImage := "openjdk:jre-alpine"
dockerExposedPorts := Seq(8080)
dockerUpdateLatest := true

/*
lazy val schema: Project = project
lazy val gateway: Project = project
  .dependsOn(schema)

lazy val `mock-shared`: Project = project
lazy val `mock-robot`: Project = project
  .dependsOn(gateway, `mock-shared`)
lazy val `mock-galahad`: Project = project
  .dependsOn(gateway, `mock-shared`)

lazy val root: Project = project.in(file("."))
  .aggregate(`mock-galahad`, `mock-robot`)
  */