package com.qoid.bennu.session

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.Config
import com.qoid.bennu.model.id.AuthenticationId
import com.qoid.bennu.security.AuthenticationManager
import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import m3.LockFreeMap
import m3.predef._
import m3.predef.box._
import m3.servlet.HttpResponseException
import m3.servlet.HttpStatusCodes
import m3.servlet.longpoll.ChannelId

import scala.collection.JavaConversions._
import scala.collection.concurrent

@Singleton
class SessionManager @Inject()(
  injector: ScalaInjector,
  authMgr: AuthenticationManager,
  config: Config
) {

  private val sessions = LockFreeMap[ChannelId, Session]()
  private val timer = new HashedWheelTimer(10, TimeUnit.SECONDS)
  private val timeouts: concurrent.Map[ChannelId, Timeout] = new ConcurrentHashMap[ChannelId, Timeout]

  def createSession(authenticationId: AuthenticationId, password: String): Box[Session] = {
    authMgr.authenticate(authenticationId, password).map { aliasIid =>
      val session = new Session(injector, aliasIid)
      sessions.put(session.channel.id, session)
      timeouts.put(session.channel.id, createTimeout(session.channel.id))
      session
    } ?~ s"failed to authenticate $authenticationId"
  }

  def getSession(channelId: ChannelId): Session = {
    sessions.get(channelId) match {
      case Some(session) =>
        timeouts.put(session.channel.id, createTimeout(session.channel.id)).foreach(_.cancel())
        session

      case None =>
        throw new HttpResponseException(HttpStatusCodes.UNAUTHORIZED)
    }
  }

  def closeSession(channelId: ChannelId): Unit = {
    sessions.remove(channelId).foreach(_.close())
    timeouts.remove(channelId)
  }

  private def createTimeout(channelId: ChannelId): Timeout = {
    timer.newTimeout(getTimerTask(channelId), config.sessionTimeoutSeconds, TimeUnit.MINUTES)
  }

  private def getTimerTask(channelId: ChannelId): TimerTask = {
    new TimerTask {
      override def run(t: Timeout): Unit = {
        closeSession(channelId)
      }
    }
  }
}
