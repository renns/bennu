package com.qoid.bennu.model

import m3.Logging
import com.google.inject.Singleton
import java.sql.{ Connection => JdbcConn }
import m3.predef._

@Singleton
class NotificationListener extends Logging {

  case class Listener(kind: String, fn: Notification => Unit)

  private lazy val _listeners: List[Listener] = List(
    Listener("ping", listenForPing) 
  )
  
  private lazy val _listenersByKind = _listeners.groupBy(_.kind)
  
  
  def fireNotification(n: Notification) = {
    _listenersByKind.get(n.kind).getOrElse(Nil).foreach(_.fn(n))
  }
  
  
  
  def listenForPing(ping: Notification): Unit = {
    implicit val jdbcConn = inject[JdbcConn]
    val conn = Connection.fetch(ping.fromConnectionIid)
    
    Notification.sendNotification(
        toPeer = conn.remotePeerId,
        kind = "pong", 
        data = ping.data
    )
  }
  

}
