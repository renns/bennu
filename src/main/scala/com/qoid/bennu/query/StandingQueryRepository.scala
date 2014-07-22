package com.qoid.bennu.query

import com.google.inject.Singleton
import com.qoid.bennu.model.id.AgentId
import com.qoid.bennu.model.id.DistributedMessageId
import com.qoid.bennu.model.id.InternalId
import m3.predef._

import scala.collection.mutable

@Singleton
class StandingQueryRepository {
  private val standingQueries = mutable.ListBuffer.empty[StandingQuery]

  def save(standingQuery: StandingQuery): Unit = {
    standingQueries.synchronized {
      standingQueries += standingQuery
    }
  }

  def find(agentId: AgentId, tpe: String): List[StandingQuery] = {
    standingQueries.filter(sq => sq.agentId == agentId && sq.tpe =:= tpe).toList
  }

  def delete(agentId: AgentId, messageId: DistributedMessageId, replyRoute: List[InternalId]): Unit = {
    standingQueries.synchronized {
      standingQueries
        .filter(sq => sq.agentId == agentId && sq.messageId == messageId && sq.replyRoute == replyRoute)
        .foreach(standingQueries -= _)
    }
  }
}
