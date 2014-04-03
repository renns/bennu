package com.qoid.bennu.squery

import com.qoid.bennu.model._
import java.sql.{ Connection => JdbcConn }
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import m3.Txn
import m3.predef._

object SqueryEvalThread extends Logging {

  case class QueueEntry(action: StandingQueryAction, instance: HasInternalId)
  
  private val _eventQueue = new LinkedBlockingQueue[QueueEntry]
  
  lazy val standingQueryManager = inject[StandingQueryManager]
  
  spawn("squery-evaluator") {
    
    while ( true ) {
      try {
        _eventQueue.poll(10, TimeUnit.SECONDS) match {
          case null =>
          case e => process(e)
        }
      } catch {
        case th: Throwable =>
          logger.error(th)
      }
    }
    
  }
  
  def enqueue(action: StandingQueryAction, instance: HasInternalId): Unit = _eventQueue.put(QueueEntry(action, instance))

  private implicit def jdbcConn = inject[JdbcConn]
  
  private def process(e: QueueEntry) = {
    try {
      Txn {
      
        standingQueryManager.notify(e.instance, e.action)
        
        cascadeToRelations(e.instance).foreach {
          case None =>
          case Some(i) =>
            standingQueryManager.notify(i, StandingQueryAction.Update)
        }
      
      }
    } catch {
      case th: Throwable => logger.error(s"error processing ${e}", th)
    }
  }
  
  def cascadeToRelations(i: HasInternalId): List[Option[HasInternalId]] = {
    i match {
      case lc: LabelChild => List(
        Label.fetchOpt(lc.parentIid),
        Label.fetchOpt(lc.childIid)
      )
      case lc: LabeledContent => List(
        Label.fetchOpt(lc.labelIid),
        Content.fetchOpt(lc.contentIid)
      )
      case la: LabelAcl => List(
        Label.fetchOpt(la.labelIid)
      )
      case _ => Nil
    }
  }
  
}