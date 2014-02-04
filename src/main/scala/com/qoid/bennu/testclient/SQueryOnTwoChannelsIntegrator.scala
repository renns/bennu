package com.qoid.bennu.testclient

import com.qoid.bennu.model.AgentId
import m3.json.LiftJsonAssist._
import m3.guice.GuiceApp
import net.model3.logging.LoggerHelper
import com.qoid.bennu.model.InternalId
import net.model3.lang.TimeDuration

/**
 * 
 * A script to test the standing query by running a few inserts (need to manually confirm via the logs that things worked)
 * 
 * + creates a channel and starts polling
 * + registers an squery to listen for changes to label and labelchild
 * + inserts a parent label, a child label and a labelchild to connect them
 * + 
 */
object SQueryOnTwoChannelsIntegrator extends GuiceApp with HttpAssist {
  
  LoggerHelper.getLogger()
  
  implicit val config = HttpAssist.HttpClientConfig()
  implicit val agentId = AgentId("007")
  
  val channel1 = createChannel
  val channel2 = createChannel
  
  spawnLongPoller(channel1, config)
  spawnLongPoller(channel2, config)
  
  registerStandingQuery(List("label"))(channel1, config)
  registerStandingQuery(List("label"))(channel2, config)
  
  val labelIid = InternalId.random
  
  post(
    path = "/api/channel/submit",
    jsonBody = parseJson(s"""
{
  "channel":"${channel1.value}",
  "requests":[
    {
      "path": "/api/upsert", 
      "context": "insert_label", 
      "parms": {
        "type": "label",
        "instance": {
          "agentId": "${agentId.value}",
          "iid": "${labelIid.value}",
          "data": "{}",
          "name": "parent"
        }
      } 
    }
  ]
}
    """.trim)
  )
  
  new TimeDuration("10 seconds").sleep
  
  System.exit(0)
  
}