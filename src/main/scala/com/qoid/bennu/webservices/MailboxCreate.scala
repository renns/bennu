package com.qoid.bennu.webservices

import com.google.inject.Inject
import com.qoid.bennu.webservices.longpoll.MailboxId
import com.qoid.bennu.webservices.longpoll.Mailbox

case class MailboxCreate @Inject() () {

  def service = {
    val id = MailboxId.random()
    Mailbox(id)
    id.value
  }
  
}