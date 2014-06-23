package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.session.SessionManager
import m3.json.JsonSerializer
import m3.json.LiftJsonAssist._
import m3.servlet.beans.JsonRequestBody
import m3.servlet.beans.MultiRequestHandler.MethodInvocation
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelId
import m3.servlet.longpoll.webservice

case class SubmitChannelRequests @Inject() (
  sessionMgr: SessionManager,
  serializer: JsonSerializer,
  processor: webservice.SubmitChannelRequests.RequestProcessor,
  requestBody: JsonRequestBody,
  @Parm("channel") channelId: ChannelId,
  @Parm("requests") requestsJv: JValue
) {

  def service(): Unit = {
    val session = sessionMgr.getSession(channelId)
    val requests = serializer.fromJson[List[MethodInvocation]](requestsJv)
    processor.submit(session.channel, requests)
  }
}
