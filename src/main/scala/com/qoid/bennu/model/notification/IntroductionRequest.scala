package com.qoid.bennu.model.notification

import com.qoid.bennu.JsonAssist
import com.qoid.bennu.JsonCapable
import com.qoid.bennu.model.InternalId
import net.liftweb.json._

object IntroductionRequest {
  def fromJson(jv: JValue): IntroductionRequest = JsonAssist.serializer.fromJson[IntroductionRequest](jv)
}

case class IntroductionRequest(
  introductionIid: InternalId,
  message: String
) extends JsonCapable
