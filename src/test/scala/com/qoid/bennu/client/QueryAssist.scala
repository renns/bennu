package com.qoid.bennu.client

import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.Alias
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

  def getRootAlias(): Future[Alias] = {
    async {
      val agent = await(getAgent())
      await(query[Alias](sql"iid = ${agent.aliasIid}")).head
    }
  }

  def getRootLabel(): Future[Label] = {
    async {
      val rootAlias = await(getRootAlias())
      await(query[Label](sql"iid = ${rootAlias.labelIid}")).head
    }
  }
}
