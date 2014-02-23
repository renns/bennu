package com.qoid.bennu.squery

import m3.predef._
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit
import com.qoid.bennu.model.HasInternalId
import com.qoid.bennu.JdbcAssist.BennuMapperCompanion
import m3.Txn
import java.sql.{ Connection => JdbcConn }
import com.qoid.bennu.model.LabelChild
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.LabeledContent
import com.qoid.bennu.model.LabelAcl
import com.qoid.bennu.model.Content

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
  
  private def process(e: QueueEntry) = Txn {
    
    standingQueryManager.notify(e.instance.mapper.asInstanceOf[BennuMapperCompanion[HasInternalId]], e.instance, e.action)
    
    cascadeToRelations(e.instance).foreach {
      case None =>
      case Some(i) =>
        standingQueryManager.notify(i.mapper.asInstanceOf[BennuMapperCompanion[HasInternalId]], i, StandingQueryAction.Update)
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