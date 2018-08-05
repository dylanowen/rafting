package com.dylowen.rafting.servicediscovery

import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import akka.actor.{ActorRef, ActorSystem, Cancellable, Props}
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Aug-2018
  */
object DockerService {

  case class HostName(name: String) {
    override def toString: String = name
  }

  case class Result(me: Int, otherInstances: Set[Int])

}

class DockerService(network: DockerNetwork, name: String)(implicit actorSystem: ActorSystem) extends LazyLogging {

  import DockerService._
  import actorSystem.dispatcher

  private val scheduled: AtomicReference[Cancellable] = new AtomicReference[Cancellable]()

  private val _me: AtomicInteger = new AtomicInteger(-1)
  private val _otherInstances: TrieMap[Int, Unit] = TrieMap()

  private val lookupActor: ActorRef = actorSystem.actorOf(Props(new ServiceActor(prefix)))

  def me: Either[ServiceDiscoveryError, HostName] = {
    val index: Int = _me.get()
    if (index >= 1) {
      Right(getHostName(index))
    }
    else {
      Left(CantFindMe)
    }
  }

  def otherInstances: Set[HostName] = {
    _otherInstances.keySet.map(getHostName).toSet
  }

  private def prefix: String = {
    s"${network.name}_${name}_"
  }

  private def getHostName(index: Int): HostName = {
    HostName(prefix + index)
  }

  def start(): Future[HostName] = {
    val next: Cancellable = actorSystem.scheduler.schedule(
      initialDelay = RefreshFrequency,
      interval = RefreshFrequency
    )({
      refreshCache()
    })

    // cancel any old schedules
    Option(scheduled.getAndSet(next))
      .map(_.cancel())

    refreshCache()
  }

  def stop(): Unit = {
    Option(scheduled.get())
      .map(_.cancel())
  }

  def refreshCache(): Future[HostName] = {
    implicit val timeout: Timeout = RefreshFrequency

    val lookup: Future[HostName] = (lookupActor ? ServiceActor.RefreshCache)
      .flatMap({
        case Result(me: Int, hosts: Set[Int]) => {
          _me.set(me)
          hosts.foreach((hostIndex: Int) => {
            _otherInstances.put(hostIndex, ())
          })

          if (me > 0) {
            Future.successful(getHostName(me))
          }
          else {
            Future.failed(CantFindMe.exception)
          }
        }
        case unexpected => {
          logger.warn("unexpected result " + unexpected)

          Future.failed(new ServiceDiscoveryException("unexpected result " + unexpected))
        }
      })

    lookup
  }
}