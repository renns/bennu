package com.qoid.bennu.webservices.v1

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.session.Session
import m3.json.JsonSerializer
import m3.predef._
import m3.servlet.beans.JsonRequestBody
import m3.servlet.beans.MultiRequestHandler.MethodInvocation
import m3.servlet.beans.Parm
import m3.servlet.longpoll.webservice

case class SubmitChannelRequests @Inject()(
  session: Session,
  serializer: JsonSerializer,
  processor: webservice.SubmitChannelRequests.RequestProcessor,
  requestBody: JsonRequestBody,
  @Parm("requests") requestsJson: JValue
) extends Logging {

  def doPost(): Unit = {
    val requests = serializer.fromJson[List[MethodInvocation]](requestsJson)
    processor.submit(session.channel, requests)
  }
}
