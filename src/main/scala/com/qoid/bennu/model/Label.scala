package com.qoid.bennu.model

import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.model.id._
import java.sql.{ Connection => JdbcConn }
import m3.jdbc._
import net.liftweb.json._

object Label extends BennuMapperCompanion[Label]

case class Label(
  name: String,
  agentId: AgentId = AgentId(""),
  @PrimaryKey iid: InternalId = InternalId.random,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Label] { self =>
  
  type TInstance = Label
  
  def mapper = Label
  
  override def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data, 
      deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }
  
  def findChild(childLabelName: String)(implicit conn: JdbcConn) = {
    Label.selectBox(sql"""name = ${childLabelName} and iid in (select childiid from labelchild where parentiid = ${iid})""")
  }
  
}
