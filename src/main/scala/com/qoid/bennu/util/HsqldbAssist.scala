package com.qoid.bennu.util

import com.qoid.bennu.JsonAssist._

object HsqldbAssist {

  def json_str(json: String, path: String): String = try {
    val jv = parseJson(json)
    val pathPars = path.split("\\.")
    pathPars.foldLeft(jv) { case (jv, v) => jv \ v } match {
      case JString(s) => s
      case _ => null
    }
  } catch {
    case e: Exception => null
  }

  def json_int(json: String, path: String)(): java.lang.Integer = try {
    val jv = parseJson(json)
    val pathPars = path.split("\\.")
    pathPars.foldLeft(jv) { case (jv, v) => jv \ v } match {
      case JInt(i) => i.toInt
      case _ => null
    }
  } catch {
    case e: Exception => null
  }

  def json_bool(json: String, path: String)(): java.lang.Boolean = try {
    val jv = parseJson(json)
    val pathPars = path.split("\\.")
    pathPars.foldLeft(jv) { case (jv, v) => jv \ v } match {
      case JBool(b) => b
      case _ => null
    }
  } catch {
    case e: Exception => null
  }

}
