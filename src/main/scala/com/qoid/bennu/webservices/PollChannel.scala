//package com.qoid.bennu.webservices
//
//import java.util.concurrent.LinkedBlockingQueue
//import java.util.concurrent.TimeUnit
//import javax.servlet.http._
//
//import com.google.inject.Inject
//import com.qoid.bennu.JsonAssist._
//import com.qoid.bennu.session.SessionManager
//import m3.predef._
//import m3.servlet.beans.Parm
//import m3.servlet.longpoll.ChannelId
//import org.eclipse.jetty.continuation.ContinuationSupport
//
//case class PollChannel @Inject() (
//  injector: ScalaInjector,
//  req: HttpServletRequest,
//  resp: HttpServletResponse,
//  sessionMgr: SessionManager,
//  @Parm("channel") channelId: ChannelId,
//  @Parm timeoutMillis: Int,
//  @Parm byteCount: Int = 24 * 1024
//) {
//
//  def service(): Unit = {
//    val session = sessionMgr.getSession(channelId)
//    val channel = session.channel
//
//    channel.synchronized {
//      // complete any existing continuation
//      val cont = ContinuationSupport.getContinuation(req)
//
//      channel.continuationTerminateOtherPolls(req)
//
//      if (channel.queue.isEmpty && cont.isInitial) {
//        channel.continuationSuspend(req, timeoutMillis)
//      } else {
//        channel.continuationComplete()
//        writeResponse(channel.queue, resp.getOutputStream, byteCount)
//        resp.getOutputStream.flush()
//      }
//    }
//  }
//
//  private def writeResponse(queue: LinkedBlockingQueue[JValue], out: javax.servlet.ServletOutputStream, byteCount: Int) = {
//    var bytesWritten = 0
//    var first = true
//
//    def write(s: String) = {
//      bytesWritten += s.length()
//      out.print(s)
//    }
//
//    def writeJv(jv: JValue) = {
//      if ( first) first = false
//      else write("\n,\n")
//      write(prettyPrint(jv))
//    }
//
//    write("[\n")
//
//    while ( !queue.isEmpty && bytesWritten < byteCount ) {
//      queue.poll(0, TimeUnit.SECONDS) match {
//        case null =>
//        case JNull =>
//        case JNothing =>
//        case jv: JValue => writeJv(jv)
//      }
//    }
//
//    write("\n]")
//  }
//}
