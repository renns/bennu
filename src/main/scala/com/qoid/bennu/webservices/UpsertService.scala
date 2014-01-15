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

case class UpsertService @Inject()(
  conn: Connection,
  serializer: JsonSerializer,
  @Parm("type") _type: String,
  @Parm instance: JValue
) extends Logging {

  implicit def _conn = conn

  def service = {

    val (mapper0, i0) = _type.toLowerCase match {
      case "alias" => Alias -> serializer.fromJson[Alias](instance)
      case "connection" => model.Connection -> serializer.fromJson[model.Connection](instance)
      case "content" => Content -> serializer.fromJson[Content](instance)
      case "label" => Content -> serializer.fromJson[Content](instance)
      case "labelacl" => LabelAcl -> serializer.fromJson[LabelAcl](instance)
      case "labelchild" => LabelChild -> serializer.fromJson[LabelChild](instance)
      case "labeledcontent" => LabeledContent -> serializer.fromJson[LabeledContent](instance)
      case _ => m3x.error(s"don't know how to handle type ${_type}")
    }

    val mapper = mapper0.asInstanceOf[Mapper[HasInternalId, InternalId]]
    val i = i0.asInstanceOf[HasInternalId]

    mapper.fetchOpt(i.iid) match {
      case None => mapper.insert(i)
      case Some(v) => mapper.update(v)
    }

    //TODO: Return request JSON
  }
}
