package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.model
import com.qoid.bennu.model._
import java.sql.Connection
import m3.json.JsonSerializer
import m3.predef._
import m3.servlet.beans.Parm

case class DeleteService @Inject()(
  conn: Connection,
  serializer: JsonSerializer,
  @Parm("type") _type: String,
  @Parm("primaryKey") iid: InternalId
) extends Logging {

  implicit def _conn = conn

  def service = {

    val mapper = _type.toLowerCase match {
      case "alias" => Alias
      case "connection" => model.Connection
      case "content" => Content
      case "label" => Content
      case "labelacl" => LabelAcl
      case "labelchild" => LabelChild
      case "labeledcontent" => LabeledContent
      case _ => m3x.error(s"don't know how to handle type ${_type}")
    }

    mapper.softDeleteViaKey(iid)
  }
}
