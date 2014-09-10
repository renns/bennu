package com.qoid.bennu.client

import com.qoid.bennu.model.Agent
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Connection
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.Profile
import m3.jdbc._
import scala.async.Async._
import scala.concurrent.Future

trait QueryAssist {
  this: ChannelClient =>

  def getAgent(): Future[Agent] = {
    async {
      await(query[Agent]()).head
    }
  }

  def getCurrentAliasLabel(): Future[Label] = {
    async {
      await(query[Label](sql"iid = ${alias.labelIid}")).head
    }
  }

  def getConnections(): Future[List[Connection]] = {
    async {
      await(query[Connection](sql"aliasIid = ${alias.iid}"))
    }
  }

  def getAlias(name: String): Future[Alias] = {
    async {
      val allAliases = await(query[Alias]())
      val label = await(query[Label](sql"iid in (${allAliases.map(_.labelIid)}) and name = ${name}")).head
      allAliases.find(_.labelIid == label.iid).head
    }
  }

  def getIntroducerConnection(): Future[Connection] = {
    async {
      val connections = await(query[Connection]())

      await(Future.sequence(connections.map { c =>
        async {
          (c, await(query[Profile]("name = 'Introducer'", List(c.iid))).nonEmpty)
        }
      })).find(_._2).getOrElse(throw new Exception("Unable to find introducer connection"))._1
    }
  }
}
