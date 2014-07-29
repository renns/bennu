package com.qoid.bennu.webservices.v1

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import javax.servlet.http._

import com.google.inject.Inject
import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.session.SessionManager
import m3.predef._
import m3.servlet.HttpStatusCodes
import m3.servlet.beans.Parm
import m3.servlet.longpoll.ChannelId
import org.apache.http.client.HttpResponseException
import org.eclipse.jetty.continuation.ContinuationSupport

/**
* Perform a long poll in order to receive asynchronous responses.
*
* Parameters:
* - timeoutMillis: Integer (Must be passed as a parameter in the URL)
* - byteCount: Integer (Optional)
*
* Response Values:
* - JSON (Array of responses)
*
* Error Codes:
* - timeoutMillisInvalid
* - byteCountInvalid
*/
case class PollChannel @Inject()(
  req: HttpServletRequest,
  resp: HttpServletResponse,
  sessionMgr: SessionManager,
  channelId: ChannelId,
  @Parm timeoutMillis: Int,
  @Parm byteCount: Int = 24 * 1024
) extends Logging {

  def doPost(): Unit = {
    try {
      validateParameters()

      sessionMgr.getSessionOpt(channelId) match {
        case Some(session) =>
          val channel = session.channel

          channel.synchronized {
            // complete any existing continuation
            val cont = ContinuationSupport.getContinuation(req)

            channel.continuationTerminateOtherPolls(req)

            if (channel.queue.isEmpty && cont.isInitial) {
              channel.continuationSuspend(req, timeoutMillis)
            } else {
              channel.continuationComplete()
              writeResponse(channel.queue, resp.getOutputStream, byteCount)
              resp.getOutputStream.flush()
            }
          }
        case _ =>
      }
    } catch {
      case e: BennuException =>
        throw new HttpResponseException(HttpStatusCodes.BAD_REQUEST, e.getErrorCode()).initCause(e)
    }
  }

  private def validateParameters(): Unit = {
    if (timeoutMillis < 1000) throw new BennuException(ErrorCode.timeoutMillisInvalid)
    if (byteCount < 1024) throw new BennuException(ErrorCode.byteCountInvalid)
  }

  private def writeResponse(queue: LinkedBlockingQueue[JValue], out: javax.servlet.ServletOutputStream, byteCount: Int) = {
    var bytesWritten = 0
    var first = true

    def write(s: String) = {
      bytesWritten += s.length()
      out.print(s)
    }

    def writeJv(jv: JValue) = {
      if (first) first = false
      else write("\n,\n")
      write(prettyPrint(jv))
    }

    write("[\n")

    while (!queue.isEmpty && bytesWritten < byteCount) {
      queue.poll(0, TimeUnit.SECONDS) match {
        case null =>
        case JNull =>
        case JNothing =>
        case jv: JValue => writeJv(jv)
      }
    }

    write("\n]")
  }
}
