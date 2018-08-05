package com.dylowen.rafting.servicediscovery

import akka.actor.ActorSystem

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Aug-2018
  */
case class DockerNetwork(name: String) {
  def apply(serviceName: String)(implicit actorSystem: ActorSystem): DockerService = {
    new DockerService(this, serviceName)
  }
}
