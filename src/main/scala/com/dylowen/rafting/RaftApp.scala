package com.dylowen.rafting

import java.net.InetAddress

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.server.Directives.{complete, getFromResource, pathPrefix}
import akka.http.scaladsl.server.PathMatchers.PathEnd
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Jul-2018
  */
object RaftApp {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("Rafting")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    import materializer.executionContext

    val host: String = "0.0.0.0"
    val port: Int = 8080

    //val root: Resource = Root.common
    val handler: Route = pathPrefix(PathEnd) {
      println(s"Called $hostname")
      complete(200, "ready")
    }

    Http().bindAndHandle(handler, host, port)

    println(s"$hostname ready")
  }

  private lazy val hostname: String = {
    InetAddress.getLocalHost.getHostName
  }
}
