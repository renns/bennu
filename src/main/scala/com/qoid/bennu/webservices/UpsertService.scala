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

case class UpsertService @Inject()(
  conn: Connection,
  serializer: JsonSerializer,
  requestBody: JsonRequestBody,
  @Parm("type") _type: String,
  @Parm instance: JValue
) extends Logging {

  implicit def _conn = conn

  def service: JValue = {

    val (mapper0, i0) = _type.toLowerCase match {
      case "alias" => Alias -> serializer.fromJson[Alias](instance)
      case "connection" => model.Connection -> serializer.fromJson[model.Connection](instance)
      case "content" => Content -> serializer.fromJson[Content](instance)
      case "label" => Label -> serializer.fromJson[Label](instance)
      case "labelacl" => LabelAcl -> serializer.fromJson[LabelAcl](instance)
      case "labelchild" => LabelChild -> serializer.fromJson[LabelChild](instance)
      case "labeledcontent" => LabeledContent -> serializer.fromJson[LabeledContent](instance)
      case _ => m3x.error(s"don't know how to handle type ${_type}")
    }

    val mapper = mapper0.asInstanceOf[Mapper[HasInternalId, InternalId]]
    val i = i0.asInstanceOf[HasInternalId]

    mapper.fetchOpt(i.iid) match {
      case None => mapper.insert(i)
      case Some(_) => mapper.update(i)
    }

    requestBody.body
  }
}
