package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JdbcAssist
import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import com.qoid.bennu.model._
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.SecurityContext
import java.sql.{ Connection => JdbcConn }
import m3.Txn
import m3.predef._
import m3.servlet.beans.Parm
import m3.json.LiftJsonAssist._
import scala.language.existentials

case class UpsertService @Inject()(
  injector: ScalaInjector,
  securityContext: SecurityContext,
  @Parm("type") tpe: String,
  @Parm("instance") instanceJson: JValue,
  @Parm parentIid: Option[InternalId] = None,
  @Parm profileName: Option[String] = None,
  @Parm profileImgSrc: Option[String] = None,
  @Parm labelIids: Option[List[InternalId]] = None
) extends Logging {

  def service: JValue = {
    val av = injector.instance[AgentView]
    implicit val jdbcConn = injector.instance[JdbcConn]
    implicit val mapper = JdbcAssist.findMapperByTypeName(tpe).asInstanceOf[BennuMapperCompanion[HasInternalId]]
    val instance = mapper.fromJson(instanceJson)

    parentIid.foreach(Txn.set(LabelChild.parentIidAttrName, _))
    profileName.foreach(Txn.set(Profile.nameAttrName, _))
    profileImgSrc.foreach(Txn.set(Profile.imgSrcAttrName, _))
    labelIids.foreach(Txn.set(LabeledContent.labelIidsAttrName, _))

    val result = mapper.fetchOpt(instance.iid) match {
      case None => av.insert(instance)
      case _ => av.update(instance)
    }

    result.toJson
  }
}
