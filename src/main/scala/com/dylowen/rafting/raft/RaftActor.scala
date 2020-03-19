package com.dylowen.rafting.raft

import akka.actor.{Actor, ActorLogging, Props}
import com.dylowen.rafting.RaftSystem
import com.dylowen.rafting.rpc.raft._

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Aug-2018
  */
object RaftActor {

  type Log[L] = List[LogEntry[L]]

  case class LogEntry[L](term: Term, command: L)

  trait LocalMessage

  case object GetLog extends LocalMessage


  private object State {
    def initial[L]: State[L] = State(
      currentTerm = 1,
      votedFor = None,
      inverseLog = List(),
      inverseCommitIndex = 0,
      inverseLastApplied = 0,
      logLength = 0
    )

    def invertIndex(index: Int): Int = index //(logLength - 1) - index
  }

  private case class State[L](currentTerm: Term,
                              votedFor: Option[Int],
                              inverseLog: Log[L],
                              inverseCommitIndex: Int,
                              inverseLastApplied: Int,
                              logLength: Int) {
    val commitIndex: Int = State.invertIndex(inverseCommitIndex)

    val lastApplied: Int = State.invertIndex(inverseLastApplied)


  }

  private case class LeaderState()

  private class RPCHandler(val handler: PartialFunction[RaftRequest, RaftResponse]) {
    // widen our definition of the handler
    def unapply(message: Any): Option[RaftResponse] = {
      message match {
        case request: RaftRequest => handler.lift(request)
        case _ => None
      }
    }
  }

  def props()(implicit system: RaftSystem): Props = Props(new RaftActor())
}

class RaftActor[L]()(implicit private val system: RaftSystem) extends Actor with ActorLogging {

  import RaftActor._

  override def receive: Receive = handleMessage(follower(State.initial))

  private def become(handler: RPCHandler): Unit = {
    context.become(handleMessage(handler))
  }

  private def handleMessage(handler: RPCHandler): Receive = {
    case handler(response) => {
      // check for an RPC call

      sender() ! response
    }
    case request => {
      log.error("Unexpected request " + request)
    }
  }

  private def follower(state: State[L]): RPCHandler = new RPCHandler({
    case AppendEntriesRequest(term, leaderId, prevLogIndex, prevLogTerm, entries, leaderCommit) => {
      if (term < state.currentTerm ||
        (state.inverseLog(State.invertIndex(prevLogIndex)).term != prevLogTerm)) {
        /*
         Reply false if term < currentTerm (§5.1) or
            if log doesn’t contain an entry at prevLogIndex whose term matches prevLogTerm (§5.3)
          */
        AppendEntriesResponse(state.currentTerm, success = false)
      }
      else {
        null
      }
    }
  })

  private def candidate(state: State[L]): RPCHandler = new RPCHandler({
    case RequestVoteRequest(term, candidateId, lastLogIndex, lastLogTerm) => {
      if (term < state.currentTerm) {
        // Reply false if term < currentTerm (§5.1) or
        RequestVoteResponse(state.currentTerm, voteGranted = false)
      }
      else if (state.votedFor.forall(_ == candidateId) && false) {
        //  If votedFor is null or candidateId, and candidate’s log is at least as up-to-date as receiver’s log, grant vote (§5.2, §5.4)
        RequestVoteResponse(state.currentTerm, voteGranted = true)
      }
      else {
        null
      }
    }
  })

  private def leader(): RPCHandler = new RPCHandler({
    case _ => null
  })
}
