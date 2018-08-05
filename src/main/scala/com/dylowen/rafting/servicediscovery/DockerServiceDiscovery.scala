package com.dylowen.rafting.servicediscovery

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