package com.dylowen.rafting.rpc.raft

import com.dylowen.rafting.raft.{LogEntry, Term}
import com.dylowen.rafting.rpc.{RPCRequestBody, RPCResponseBody}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Aug-2018
  */
sealed trait RaftRequest extends RPCRequestBody

sealed trait RaftResponse extends RPCResponseBody

case class AppendEntriesRequest(term: Term,
                                leaderId: Int,
                                prevLogIndex: Int,
                                prevLogTerm: Term,
                                entries: Seq[LogEntry],
                                leaderCommit: Int) extends RaftRequest

case class AppendEntriesResponse(term: Term,
                                 success: Boolean) extends RaftResponse


case class RequestVoteRequest(term: Term,
                              candidateId: Int,
                              lastLogIndex: Int,
                              lastLogTerm: Term) extends RaftRequest

case class RequestVoteResponse(term: Term,
                               voteGranted: Boolean) extends RaftResponse


case class InstallSnapshotRequest(term: Term,
                                  leaderId: Int,
                                  lastIncludedIndex: Int,
                                  lastIncludedTerm: Term,
                                  offset: Int,
                                  date: Array[Byte],
                                  done: Boolean) extends RaftRequest

case class InstallSnapshotResponse(term: Term) extends RaftResponse


/*
class RaftSchema {
  lazy implicit val schemaForRequest = SchemaFor[AppendEntries.Request]
  //lazy implicit val schemaForResponse = SchemaFor[RaftRPCResponse]

  lazy val Request: Schema = AvroSchema[AppendEntries.Request :+: RequestVote.Request :+: CNil]
  lazy val Response: Schema = AvroSchema[AppendEntries.Response :+: RequestVote.Response :+: CNil]
}
*/