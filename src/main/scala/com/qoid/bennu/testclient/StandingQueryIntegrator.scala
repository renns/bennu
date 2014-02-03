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
object StandingQueryIntegrator extends GuiceApp with HttpAssist {
  
  LoggerHelper.getLogger()
  
  implicit val config = HttpAssist.HttpClientConfig()
  implicit val agentId = AgentId("007")
  implicit val channel = createChannel
  
  
  spawnLongPoller
  
  val squeryHandle = registerStandingQuery(List("label","labelchild"))
  
  val parentLabelIid = InternalId.random
  val childLabelIid = InternalId.random
  val labelChildIid = InternalId.random
  
  post(
    path = "/api/channel/submit",
    channel = Some(channel),
    jsonBody = parseJson(s"""
{
  "channel":"${channel.value}",
  "requests":[
    {
      "path": "/api/upsert", 
      "context": "insert_parent", 
      "parms": {
        "type": "label",
        "instance": {
          "agentId": "${agentId.value}",
          "iid": "${parentLabelIid.value}",
          "data": "",
          "name": "parent"
        }
      } 
    },    {
      "path": "/api/upsert", 
      "context": "insert_child", 
      "parms": {
        "type": "label",
        "instance": {
          "agentId": "${agentId.value}",
          "iid": "${childLabelIid.value}",
          "data": "",
          "name": "child"
        }
      } 
    },{
      "path": "/api/upsert", 
      "context": "insert_childlabel", 
      "parms": {
        "type": "labelchild",
        "instance": {
          "agentId": "${agentId.value}",
          "iid": "${labelChildIid.value}",
          "data": "",
          "parentIid": "${parentLabelIid.value}"
          "childIid": "${childLabelIid.value}"
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