package com.dylowen.rafting

import akka.actor.ActorSystem
import akka.stream.Materializer

import scala.concurrent.ExecutionContextExecutor

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Aug-2018
  */
case class RaftSystem(me: DockerService.HostName,
                      serviceDiscovery: DockerService)
                     (implicit private val _actorSystem: ActorSystem,
                      private val _materializer: Materializer) {
  @inline
  implicit def actorSystem: ActorSystem = _actorSystem

  @inline
  implicit def materializer: Materializer = _materializer

  @inline
  implicit def executionContext: ExecutionContextExecutor = _materializer.executionContext

  def stop(): Unit = {

  }
}
