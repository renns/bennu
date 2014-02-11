package com.qoid.bennu.model


import com.qoid.bennu.JdbcAssist._
import com.qoid.bennu.squery.StandingQueryAction
import java.sql.{ Connection => JdbcConn }
import m3.jdbc._
import m3.predef._
import net.liftweb.json._

object Notification extends BennuMapperCompanion[Notification] {

  def sendNotification(toPeer: PeerId, kind: NotificationKind, data: JValue)(implicit jdbcConn: JdbcConn): Unit = {
    
    val toConn = Connection.selectBox(sql"localPeerId = ${toPeer}").open_$
    
    val notification = Notification(
      iid = InternalId.random,
      agentId = toConn.agentId,
      consumed = false,
      fromConnectionIid = toConn.iid,
      kind = kind,
      data = data
    )
    
    // insert into database
    notification.sqlInsert
    
    // let listeners know about it
    inject[NotificationListener].fireNotification(notification)
    
    // let standing queries know about it
    notification.notifyStandingQueries(StandingQueryAction.Insert)
  }
}

case class Notification(
  @PrimaryKey iid: InternalId = InternalId.random,
  agentId: AgentId,
  consumed: Boolean,
  fromConnectionIid: InternalId,  
  kind: NotificationKind,
  data: JValue = JNothing,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Notification] {
  self =>
  
  type TInstance = Notification
  
  def mapper = Notification
  
  override def copy2(
      iid: InternalId = self.iid, 
      agentId: AgentId = self.agentId, 
      data: JValue = self.data, 
      deleted: Boolean = self.deleted
  ) = {
    copy(iid = iid, agentId = agentId, data = data, deleted = deleted)
  }
}


