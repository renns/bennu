package com.qoid.bennu.client

import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.Label
import m3.jdbc._

import scala.async.Async._
import scala.concurrent.Future

trait QueryAssist {
  this: ChannelClient with ServiceAssist =>

  def getAgent(): Future[Agent] = {
    async {
      await(query[Agent]("")).head
    }
  }

  def getCurrentAlias(): Future[Alias] = {
    async {
      val agent = await(getAgent())
      await(query[Alias](sql"iid = ${agent.aliasIid}")).head
    }
  }

  def getCurrentAliasLabel(): Future[Label] = {
    async {
      val alias = await(getCurrentAlias())
      await(query[Label](sql"iid = ${alias.labelIid}")).head
    }
  }

  def getConnections(): Future[List[Connection]] = {
    async {
      val alias = await(getCurrentAlias())
      await(query[Connection](sql"aliasIid = ${alias.iid}"))
    }
  }
}
