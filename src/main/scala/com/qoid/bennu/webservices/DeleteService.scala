package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu._
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AgentView
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json._

case class DeleteService @Inject() (
  injector: ScalaInjector,
  @Parm("type") tpe: String,
  @Parm("primaryKey") iid: InternalId
) extends Logging {

  def service: JValue = {
    val av = injector.instance[AgentView]
    implicit val mapper = JdbcAssist.findMapperByTypeName(tpe).asInstanceOf[BennuMapperCompanion[HasInternalId]]
    val instance = av.fetch(iid)
    av.delete(instance).toJson
  }
}
