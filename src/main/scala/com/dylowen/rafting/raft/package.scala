package com.dylowen.rafting

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Aug-2018
  */
package object raft {
  type Term = Int

  case class LogEntry(term: Term, command: Array[Byte])
}
