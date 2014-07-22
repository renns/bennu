//package com.qoid.bennu.client
//
//import com.qoid.bennu.JsonAssist._
//import com.qoid.bennu.model._
//import com.qoid.bennu.model.id._
//import m3.LockFreeMap
//import scala.concurrent._
//
//trait ChannelClient extends ServiceAssist with ModelAssist {
//  implicit val ec: ExecutionContext
//  val rootAliasIid: InternalId
//  val agentName: String
//
//  protected val asyncCallbacks = new LockFreeMap[JValue, QueryResponse => Unit]
//
//  def post(
//    path: String,
//    parms: Map[String, JValue],
//    context: JValue = JString(InternalId.random.value)
//  ): Future[ChannelResponse]
//
//  def close(): Unit
//}
