package com.dylowen.rafting

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives.{complete, pathPrefix}
import akka.http.scaladsl.server.PathMatchers.PathEnd
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.dylowen.rafting.rpc.RPCRequest
import com.dylowen.rafting.rpc.raft.AppendEntriesRequest
import com.dylowen.rafting.servicediscovery.{DockerService, DockerServiceDiscovery}
import com.sksamuel.avro4s.{AvroInputStream, AvroOutputStream}
import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Jul-2018
  */
object RaftApp extends LazyLogging {
  def main(args: Array[String]): Unit = {
    implicit val actorSystem: ActorSystem = ActorSystem("Rafting")
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    import materializer.executionContext


    val request: RPCRequest = AppendEntriesRequest(1, 2, 1, 2, Seq(), 1)

    val tempOut = new ByteArrayOutputStream()



    val os: AvroOutputStream[RPCRequest] = AvroOutputStream.binary(tempOut)
    os.write(request)
    os.flush()
    os.close()

    val tempIn = new ByteArrayInputStream(tempOut.toByteArray)

    val is: AvroInputStream[RPCRequest] = AvroInputStream.binary(tempIn)
    val result = is.iterator.toSet.head
    is.close()

    println(result)


    val serviceDiscovery: DockerService = DockerServiceDiscovery("rafting")("raft")

    serviceDiscovery.start()
      .onComplete({
        case Success(hostname) => {
          implicit val system: RaftSystem = RaftSystem(hostname, serviceDiscovery)

          start()
        }
        case Failure(exception) => {
          logger.error("Failed to start (Probably not running in docker, try `./bin/serve`)", exception)

          shutdown(serviceDiscovery, actorSystem)
        }
      })
  }

  private def start()(implicit system: RaftSystem): Unit = {
    import system.{actorSystem, materializer}

    val host: String = "0.0.0.0"
    val port: Int = 8080

    val handler: Route = pathPrefix(PathEnd) {
      println(s"Called ${system.me}")
      complete(200, system.me + "\n" + system.serviceDiscovery.otherInstances)
    }

    Http().bindAndHandle(handler, host, port)

    // register shutdown
    sys.ShutdownHookThread({
      shutdown(system.serviceDiscovery, system.actorSystem)
    })

    logger.info(s"${system.me} is ready")
  }

  private def shutdown(serviceDiscovery: DockerService, actorSystem: ActorSystem): Unit = {
    logger.info("Shutting down")

    serviceDiscovery.stop()
    actorSystem.terminate()
  }
}
