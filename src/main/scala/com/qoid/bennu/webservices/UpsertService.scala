package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model
import com.qoid.bennu.model._
import java.sql.Connection
import m3.jdbc._
import m3.json.JsonSerializer
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json.JValue
import com.qoid.bennu.model.InternalId
import com.qoid.bennu.model.HasInternalId
import m3.servlet.beans.JsonRequestBody
import scala.language.existentials
import com.qoid.bennu.JdbcAssist

case class UpsertService @Inject()(
  conn: Connection,
  serializer: JsonSerializer,
  requestBody: JsonRequestBody,
  @Parm("type") _type: String,
  @Parm instance: JValue
) extends Logging {

  implicit def _conn = conn

  def service: JValue = {

    val mapper = JdbcAssist.findMapperByTypeName(_type).asInstanceOf[JdbcAssist.BennuMapperCompanion[HasInternalId]]
    val i = mapper.fromJson(instance)

    mapper.fetchOpt(i.iid) match {
      case None => mapper.insert(i)
      case Some(_) => mapper.update(i)
    }

    requestBody.body
  }
}
