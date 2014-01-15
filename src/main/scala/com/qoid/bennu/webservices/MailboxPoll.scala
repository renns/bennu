package com.qoid.bennu.webservices

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import com.qoid.bennu.webservices.longpoll.MailboxId
import m3.predef._
import org.eclipse.jetty.continuation.ContinuationSupport
import java.util.concurrent.TimeUnit
import com.qoid.bennu.webservices.longpoll.Response.ErrorResponse
import com.qoid.bennu.webservices.longpoll.Response.JsonResponse
import com.qoid.bennu.webservices.longpoll.MailboxId
import com.qoid.bennu.webservices.longpoll.Mailbox
import m3.servlet.beans.Parm

/**
 * This code is hopelessly tied to jetty (well not hopeless but it is tied to Jetty)
 * 
 */
case class MailboxPoll(
  req: HttpServletRequest,
  resp: HttpServletResponse,
  @Parm mailboxId: MailboxId,
  @Parm timeout: Int
) extends Logging {

  def service = {
    
    val mailbox = Mailbox(mailboxId)
    
    if ( mailbox.responseQueue.isEmpty ) {
      mailbox.synchronized {
        logger.debug(s"no records to send sleeping for 30 seconds -- ${mailboxId}")
        val continuation = ContinuationSupport.getContinuation(req)
        mailbox.continuation = Some(continuation)
        continuation.setTimeout(timeout)
        continuation.suspend()
      }
    } else {
      mailbox.responseQueue.poll(0, TimeUnit.SECONDS) match {
        case null => 
        case r: ErrorResponse => {
          ("status")
        }
        case r: JsonResponse => 
      }
    }
    
  }
  
}