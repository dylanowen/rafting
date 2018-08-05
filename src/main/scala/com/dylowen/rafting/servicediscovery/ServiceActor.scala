package com.dylowen.rafting.servicediscovery

import java.net.InetAddress

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.pattern.pipe
import com.dylowen.rafting.servicediscovery.DockerService._

import scala.concurrent.Future
import scala.util.control.NonFatal

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Aug-2018
  */
private[servicediscovery] object ServiceActor {

  private[servicediscovery] sealed trait Message

  private[servicediscovery] case object RefreshCache extends Message

  private[servicediscovery] case object ScheduleLookup extends Message

  private[servicediscovery] case class LookupResult(index: Int, success: Boolean, me: Boolean) extends Message

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

}

private[servicediscovery] class ServiceActor(prefix: String) extends Actor with ActorLogging {

  import ServiceActor._
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
