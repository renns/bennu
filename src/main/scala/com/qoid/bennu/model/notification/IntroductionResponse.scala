package com.qoid.bennu.model.notification

import com.qoid.bennu.JsonAssist
import com.qoid.bennu.JsonCapable
import com.qoid.bennu.model.InternalId
import net.liftweb.json._

object IntroductionResponse {
  def fromJson(jv: JValue): IntroductionResponse = JsonAssist.serializer.fromJson[IntroductionResponse](jv)
}

case class IntroductionResponse(
  introductionIid: InternalId,
  accepted: Boolean
) extends JsonCapable
