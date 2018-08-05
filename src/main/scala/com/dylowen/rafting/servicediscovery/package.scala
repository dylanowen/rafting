package com.dylowen.rafting

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Aug-2018
  */
package object servicediscovery {

  private[servicediscovery] val RequestTimeout: FiniteDuration = 1 second
  private[servicediscovery] val MaxRange: Int = 10
  private[servicediscovery] val RefreshFrequency: FiniteDuration = RequestTimeout.mul(MaxRange * 2)

  class ServiceDiscoveryException(message: String) extends RuntimeException(message)

  trait ServiceDiscoveryError {
    def message: String

    @inline
    def exception: RuntimeException = {
      new ServiceDiscoveryException(message)
    }
  }

  case object CantFindMe extends ServiceDiscoveryError {
    override val message: String = "Can't find me"
  }

}
