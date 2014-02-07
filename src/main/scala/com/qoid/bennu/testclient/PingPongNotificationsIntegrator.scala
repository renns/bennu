package com.qoid.bennu.testclient

import com.qoid.bennu.model._
import m3.guice.GuiceApp
import scala.collection.immutable.HashMap
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import scala.concurrent.duration.Duration
import com.qoid.bennu.JsonAssist._
import jsondsl._

object PingPongNotificationsIntegrator extends GuiceApp with HttpAssist {
  
  
  run()
  System.exit(0)

  def createClient(agentIdStr: String): (Alias, ChannelClient with SendNotification) = {
    
    val agentId = AgentId(agentIdStr)
    AgentManager.createAgent(agentId, overwrite = true)
    
    val client = ChannelClientFactory.createHttpChannelClient(agentId)
    
    // create alias here
    val f = client.createAlias(agentIdStr)
    
    val alias = Await.result(f._1, Duration("20 seconds"))
    val label = Await.result(f._2, Duration("20 seconds"))
    
    alias -> client 
    
  }
  
  
  def run(): Unit = {
    
    // This promise is completed when the whole test should be finished
    val p = Promise[Unit]()

    logger.debug("Starting ...")

    val (leftAlias, left) = createClient("007")
    val (rightAlias, right) = createClient("008")

    val leftPeerId = PeerId.random
    val rightPeerId = PeerId.random
    
    val leftConn = left.upsert(Connection(
      iid = InternalId.random,
      agentId = left.agentId,
      aliasIid = leftAlias.iid,
      localPeerId = leftPeerId,
      remotePeerId = rightPeerId,
      data = JNothing
    ))
    
    val rightConn = right.upsert(Connection(
      iid = InternalId.random,
      agentId = right.agentId,
      aliasIid = rightAlias.iid,
      localPeerId = rightPeerId,
      remotePeerId = leftPeerId,
      data = JNothing
    ))
      
    val squeryFuture = left.registerStandingQuery(List("notification")) {
      case (_, n: Notification) => {
        logger.debug(s"notification received ${n}")
        p.success()
      }
      case i => logger.debug(s"Invalid type in standing query -- ${i}")
    }

    
    for {
      rc <- rightConn 
      lc <- leftConn
    } {
      left.sendNotification(rightPeerId, "ping", ("hello" -> "world"))
    }
    
    Await.ready(p.future, Duration("30 seconds"))
    
  }
}