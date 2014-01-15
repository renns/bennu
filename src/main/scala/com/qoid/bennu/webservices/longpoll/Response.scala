package com.qoid.bennu.webservices.longpoll

import net.liftweb.json.JValue
import m3.predef._

object Response {
  
  case class ErrorResponse(
    context: JValue,
    message: String
  ) extends Response

  case class JsonResponse(
    context: JValue,
    payload: JValue
  ) extends Response
  
}


sealed trait Response {
  val context: JValue
}
