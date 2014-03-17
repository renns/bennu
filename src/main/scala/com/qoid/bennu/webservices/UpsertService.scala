package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.SecurityContext
import java.sql.{ Connection => JdbcConn }
import m3.predef._
import m3.servlet.beans.Parm
import net.liftweb.json._
import scala.language.existentials

case class UpsertService @Inject()(
  injector: ScalaInjector,
  securityContext: SecurityContext,
  @Parm("type") tpe: String,
  @Parm("instance") instanceJson: JValue
) extends Logging {

  def service: JValue = {
    val av = injector.instance[AgentView]
    implicit val jdbcConn = injector.instance[JdbcConn]
    implicit val mapper = JdbcAssist.findMapperByTypeName(tpe).asInstanceOf[BennuMapperCompanion[HasInternalId]]
    val instance = mapper.fromJson(instanceJson).copy2(agentId = securityContext.agentId).asInstanceOf[HasInternalId]

    val result = mapper.fetchOpt(instance.iid) match {
      case None => av.insert(instance)
      case _ => av.update(instance)
    }

    result.toJson
  }
}
