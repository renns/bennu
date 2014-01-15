package com.qoid.bennu.webservices.longpoll

import java.util.concurrent.LinkedBlockingQueue
import m3.StringConverters.HasStringConverter
import m3.StringConverters.Converter
import net.model3.util.UidGenerator
import com.qoid.bennu.JsonAssist._
import m3.predef._
import java.util.concurrent.ConcurrentHashMap
import org.eclipse.jetty.continuation.Continuation
import scala.collection.JavaConverters._

object Mailbox {
  
  val requestQueue = new LinkedBlockingQueue[(Mailbox,Request)]
  
  // we use java.util because it is very fast
  val mailboxes = new ConcurrentHashMap[MailboxId,Mailbox]().asScala

  def apply(id: MailboxId): Mailbox = {
    mailboxes.get(id).getOrElse{
      mailboxes.synchronized {
        mailboxes.get(id).getOrElse {
          val mb = new Mailbox(id)
          mailboxes.put(id, mb)
          mb
        }
      }
    }
  }
  
  
}

class Mailbox(id: MailboxId) {
  
  @volatile private var _continuation = none[Continuation]
  
  def continuation = _continuation
  def continuation_=(c: Option[Continuation]): Unit = synchronized {
    _continuation.foreach(_.complete())
    _continuation = c
  }

  val responseQueue = new LinkedBlockingQueue[Response]()
  
}
