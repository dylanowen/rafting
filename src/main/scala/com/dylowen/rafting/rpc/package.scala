package com.dylowen.rafting

import com.dylowen.rafting.rpc.raft._
import com.sksamuel.avro4s.AvroSchema
import org.apache.avro.Schema
import shapeless.ops.coproduct.Inject
import shapeless.{:+:, CNil, Coproduct}

import scala.language.implicitConversions

/**
  * TODO add description
  *
  * @author dylan.owen
  * @since Aug-2018
  */
package object rpc {

  trait RPCRequestBody

  trait RPCResponseBody

  private type Req = AppendEntriesRequest :+:
    RequestVoteRequest :+:
    InstallSnapshotRequest :+:
    CNil

  private type Res = AppendEntriesResponse :+:
    RequestVoteResponse :+:
    InstallSnapshotResponse :+:
    CNil

  object RPCRequest {

    def apply[R <: RPCRequestBody](body: R)(implicit inj: Inject[Req, R]): RPCRequest = {
      RPCRequest(Coproduct[Req](body))
    }

    implicit def body2Req[R <: RPCRequestBody](body: R)(implicit inj: Inject[Req, R]): RPCRequest = {
      apply(body)
    }

    implicit def req2Body(request: RPCRequest): Req = request.body
  }

  case class RPCRequest(body: Req)

  object RPCResponse {

    def apply[R <: RPCResponseBody](body: R)(implicit inj: Inject[Res, R]): RPCResponse = {
      RPCResponse(Coproduct[Res](body))
    }

    implicit def body2Res[R <: RPCResponseBody](body: R)(implicit inj: Inject[Res, R]): RPCResponse = {
      RPCResponse(body)
    }

    implicit def res2Body(response: RPCResponse): Res = response.body
  }

  case class RPCResponse(body: Res)

  lazy val _RequestSchema: Schema = AvroSchema[RPCRequest]
  lazy val _ResponseSchema: Schema = AvroSchema[RPCResponse]

  @inline
  implicit def RequestSchema: Schema = _RequestSchema

  @inline
  implicit def ResponseSchema: Schema = _ResponseSchema
}
