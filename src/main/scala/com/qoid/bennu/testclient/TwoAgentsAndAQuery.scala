package com.qoid.bennu.testclient

import com.qoid.bennu.model.AgentId
import m3.json.LiftJsonAssist._
import m3.guice.GuiceApp
import net.model3.logging.LoggerHelper
import com.qoid.bennu.model.InternalId
import net.model3.lang.TimeDuration

/**
 * 
 * Creates two agents then runs a label query to ensure we only see labels from the
 * agent we requested labels for
 * + 
 */
object TwoAgentsAndAQuery extends GuiceApp with HttpAssist {
  
  LoggerHelper.getLogger()

  lazy val doubleZeroEight = AgentId("008")
  createAgent(doubleZeroEight)
  
  implicit lazy val config = HttpAssist.HttpClientConfig()
  
  implicit lazy val agentId = AgentId("007")
  createAgent(agentId)
  
  implicit lazy val channel = createChannel(agentId, config)
  
  spawnLongPoller
  
  // query labels we should only see 007's labels
  httpPost(
    path = "/api/channel/submit",
    channel = Some(channel),
    jsonBody = parseJson(s"""
{
  "channel":"${channel.value}",
  "requests":[
    {
      "path": "/api/query", 
      "context": "queray_007_labels_there_should_be_only_one", 
      "parms": {
        "type": "label",
        "q": "1=1"
      }
    }
  ]
}
    """.trim)
  )
  
  new TimeDuration("10 seconds").sleep
  
  System.exit(0)
  
}