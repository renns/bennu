package com.qoid.bennu.model


import com.qoid.bennu.JdbcAssist._
import m3.jdbc.PrimaryKey
import net.liftweb.json._
import m3.predef._
import m3.jdbc._
import java.sql.{ Connection => JdbcConn }
import com.qoid.bennu.squery.StandingQueryManager
import com.qoid.bennu.squery.StandingQueryAction
import m3.Txn

object Notification extends BennuMapperCompanion[Notification] {

  def sendNotification(toPeer: PeerId, kind: String, data: JValue)(implicit jdbcConn: JdbcConn) = {
    
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
    inject[StandingQueryManager].notify(
      Notification,
      notification,
      StandingQueryAction.Insert
    )
    
  }
  
}

case class Notification(
  @PrimaryKey iid: InternalId,
  agentId: AgentId,
  consumed: Boolean,
  fromConnectionIid: InternalId,  
  kind: String,
  data: JValue,
  deleted: Boolean = false
) extends HasInternalId with BennuMappedInstance[Notification] {
  def mapper = Notification
}


