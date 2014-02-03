package com.qoid.bennu.testclient

import com.qoid.bennu.model.AgentId
import m3.json.LiftJsonAssist._
import m3.guice.GuiceApp
import net.model3.logging.LoggerHelper
import com.qoid.bennu.model.InternalId
import net.model3.lang.TimeDuration

object StandingQueryIntegrator extends GuiceApp with HttpAssist {
  
  LoggerHelper.getLogger()
  
  implicit val config = HttpAssist.Config()
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
      "path": "/api/delete", 
      "context": "insert_childlabel", 
      "parms": {
        "type": "labelchild",
        "instance": {
          "agentId": "${agentId.value}",
          "iid": "${labelChildIid.value}",
          "data": "",
          "parentLabel": "${parentLabelIid.value}"
          "childLabel": "${childLabelIid.value}"
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