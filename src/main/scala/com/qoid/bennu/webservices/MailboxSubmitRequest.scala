package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.JsonAssist._
import m3.predef._
import com.qoid.bennu.webservices.longpoll.MailboxId
import com.qoid.bennu.webservices.longpoll.Mailbox
import com.qoid.bennu.webservices.longpoll.Request
import com.qoid.bennu.webservices.longpoll.MailboxId
import com.qoid.bennu.webservices.longpoll.Request
import m3.servlet.beans.Parm


case class MailboxSubmitRequests @Inject() (
  @Parm responseMailbox: MailboxId,
  @Parm requests: List[Request]
) {
  
  def service = {
    val mailbox = Mailbox.mailboxes.get(responseMailbox).getOrError(s"invalid mailbox id ${responseMailbox}")
    requests.foreach { r =>
      Mailbox.requestQueue.put(mailbox -> r)
    }
  }
  
}