package com.qoid.bennu.webservices.longpoll

import java.util.concurrent.LinkedBlockingQueue
import m3.StringConverters.HasStringConverter
import m3.StringConverters.Converter
import net.model3.util.UidGenerator
import com.qoid.bennu.JsonAssist._
import m3.predef._
import java.util.concurrent.ConcurrentHashMap

object MailboxId extends HasStringConverter {

  val stringConverter = new Converter[MailboxId] {
    override def toString(value: MailboxId) = value.value
    def fromString(value: String) = MailboxId(value)
  }
  
  val uidGenerator = inject[UidGenerator]

  def random(): MailboxId = MailboxId(uidGenerator.create(32))
  
}

case class MailboxId(value: String)
