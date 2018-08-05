package com.dylowen.rafting.rpc.raft

import com.dylowen.rafting.rpc.{RPCRequestBody, RPCResponseBody}

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Aug-2018
  */
case class AppendEntriesRequest(term: Term,
                                leaderId: Int,
                                prevLogIndex: Int,
                                prevLogTerm: Term,
                                entries: Seq[String],
                                leaderCommit: Int) extends RPCRequestBody

case class AppendEntriesResponse(term: Term,
                                 success: Boolean) extends RPCResponseBody


case class RequestVoteRequest(term: Term,
                              candidateId: Int,
                              lastLogIndex: Int,
                              lastLogTerm: Term) extends RPCRequestBody

case class RequestVoteResponse(term: Term,
                               voteGranted: Boolean) extends RPCResponseBody


/*
class RaftSchema {
  lazy implicit val schemaForRequest = SchemaFor[AppendEntries.Request]
  //lazy implicit val schemaForResponse = SchemaFor[RaftRPCResponse]

  lazy val Request: Schema = AvroSchema[AppendEntries.Request :+: RequestVote.Request :+: CNil]
  lazy val Response: Schema = AvroSchema[AppendEntries.Response :+: RequestVote.Response :+: CNil]
}
*/