package com.qoid.bennu.query

import com.qoid.bennu.ScalaUnitTest
import com.qoid.bennu.model._
import m3.servlet.longpoll.ChannelId
import org.junit.Test

//class StandingQueryManagerTest extends ScalaUnitTest {
  //test getting empty list

//  @Test
//  def testAddGetSingle(): Unit = {
//    val sQueryMgr = new StandingQueryManager
//
//    val agentId = AgentId("agent")
//    val channel = ChannelId("channel")
//    val handle = InternalId("handle")
//    val tpe = "type"
//    val types = Set(tpe)
//
//    val sQuery = new StandingQuery(agentId, channel, handle, types)
//
//    sQueryMgr.add(sQuery)
//
//    val result = sQueryMgr.get(agentId, tpe)
//
//    assert(List(sQuery) === result)
//  }
//
//  @Test
//  def testAddGetMultiple(): Unit = {
//    val sQueryMgr = new StandingQueryManager
//
//    val agentId = AgentId("agent")
//    val channel = ChannelId("channel")
//    val handle1 = InternalId("handle1")
//    val handle2 = InternalId("handle2")
//    val tpe = "type"
//    val types = Set(tpe)
//
//    val sQuery1 = new StandingQuery(agentId, channel, handle1, types)
//    val sQuery2 = new StandingQuery(agentId, channel, handle2, types)
//
//    sQueryMgr.add(sQuery1)
//    sQueryMgr.add(sQuery2)
//
//    val result = sQueryMgr.get(agentId, tpe)
//
//    assert(List(sQuery1, sQuery2) === result)
//  }
//
//  @Test
//  def testRemove(): Unit = {
//    val sQueryMgr = new StandingQueryManager
//
//    val agentId = AgentId("agent")
//    val channel = ChannelId("channel")
//    val handle = InternalId("handle")
//    val tpe = "type"
//    val types = Set(tpe)
//
//    val sQuery = new StandingQuery(agentId, channel, handle, types)
//
//    sQueryMgr.add(sQuery)
//
//    val result1 = sQueryMgr.get(agentId, tpe)
//
//    assert(List(sQuery) === result1)
//
//    sQueryMgr.remove(agentId, handle)
//
//    val result2 = sQueryMgr.get(agentId, tpe)
//
//    assert(Nil === result2)
//  }
//}
