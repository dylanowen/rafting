package com.dylowen.rafting

import java.net.InetAddress
import java.util.concurrent.atomic.{AtomicInteger, AtomicReference}

import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Cancellable, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging

import scala.collection.concurrent.TrieMap
import scala.concurrent.Future
import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps
import scala.util.control.NonFatal
import scala.util.{Failure, Success}

/**
  * Hacky service discovery based on docker compose host names
  *
  * @author dylan.owen
  * @since Jul-2018
  */
object DockerServiceDiscovery {
  def apply(networkName: String): DockerNetwork = {
    DockerNetwork(networkName)
  }
}

case class DockerNetwork(name: String) {
  def apply(serviceName: String)(implicit actorSystem: ActorSystem): DockerService = {
    new DockerService(this, serviceName)
  }
}

case class ServiceDiscoveryError(message: String)

object DockerService {
  private val RequestTimeout: FiniteDuration = 1 second
  private val MaxRange: Int = 10
  private val RefreshFrequency: FiniteDuration = RequestTimeout.mul(MaxRange * 2)

  case class HostName(name: String) {
    override def toString: String = name
  }

  sealed trait Message

  case object RefreshCache extends Message

  private case object ScheduleLookup extends Message

  private case class LookupResult(index: Int, success: Boolean, me: Boolean) extends Message

  private trait LookupStatus {
    def found: Boolean

    def success: Boolean

    def me: Boolean
  }

  private case object StatusLooking extends LookupStatus {
    override def found: Boolean = false

    override def success: Boolean = false

    override def me: Boolean = false
  }

  private case class StatusFound(override val success: Boolean, override val me: Boolean) extends LookupStatus {
    override def found: Boolean = true
  }

  case class Result(me: Int, otherInstances: Set[Int])

  class ServiceActor(prefix: String) extends Actor with ActorLogging {

    import context.dispatcher

    override def receive: Receive = ready

    private def ready: Actor.Receive = {
      case RefreshCache => {
        // setup our state complete with skipping 0
        context.become(running(Set(sender()), InetAddress.getLocalHost, Vector(StatusFound(success = false, me = false))))

        for (_ <- 1 to MaxRange) {
          self ! ScheduleLookup
        }
      }
      case _ => // ignore late responses
    }

    private def running(requesters: Set[ActorRef], myAddresss: InetAddress, lastStatuses: Vector[LookupStatus]): Actor.Receive = {
      case ScheduleLookup => {
        context.become(running(requesters, myAddresss, lastStatuses :+ StatusLooking))
        lookupHost(lastStatuses.length, myAddresss)
      }
      case LookupResult(index, success, me) => {
        val statuses: Vector[LookupStatus] = lastStatuses.updated(index, StatusFound(success, me))

        // modify our state
        context.become(running(requesters, myAddresss, statuses))

        val longestFailureRun: Int = statuses.foldRight((0, 0))((status: LookupStatus, count: (Int, Int)) => {
          val current: Int = if (status.found && !status.success) {
            count._1 + 1
          }
          else {
            0
          }

          (current, math.max(current, count._2))
        })._2

        if (longestFailureRun >= MaxRange) {
          val result: Result = statuses.zipWithIndex.filter(_._1.success)
            .foldLeft(Result(-1, Set()))((tempResult: Result, status: (LookupStatus, Int)) => {
              if (status._1.me) {
                tempResult.copy(me = status._2)
              }
              else {
                tempResult.copy(otherInstances = tempResult.otherInstances + status._2)
              }
            })

          // stop iterating
          context.become(ready)

          // send our response
          requesters.foreach(_ ! result)
        }
        else {
          // schedule our next lookup
          self ! ScheduleLookup
        }
      }
      case RefreshCache => {
        context.become(running(requesters + sender(), myAddresss, lastStatuses))
      }
      case unexpected => {
        log.warning("unexpected: " + unexpected)
      }
    }

    private def lookupHost(index: Int, myAddress: InetAddress): Unit = {
      val result: Future[LookupResult] = Future({
          val host: String = prefix + index
          val address: InetAddress = InetAddress.getByName(host)
          val isMe: Boolean = address.equals(myAddress)

          LookupResult(index, address.isReachable(RequestTimeout.toMillis.asInstanceOf[Int]), isMe)
        })
        .recover({
          case NonFatal(_) => {
            LookupResult(index, success = false, me = false)
          }
        })

      result pipeTo self
    }
  }

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
      Left(ServiceDiscoveryError("Can't find me"))
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

  def start(): Future[Unit] = {
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

  def refreshCache(): Future[Unit] = {
    implicit val timeout: Timeout = RefreshFrequency

    val lookup: Future[Any] = lookupActor ? RefreshCache

    lookup.onComplete({
      case Success(Result(me: Int, hosts: Set[Int])) => {
        _me.set(me)
        hosts.foreach((hostIndex: Int) => {
          _otherInstances.put(hostIndex, ())
        })
      }
      case Success(v) => logger.warn("unexpected result " + v)
      case Failure(exception) => logger.error("Lookup actor error", exception)
    })

    lookup.map(_ => (): Unit)
  }
}
