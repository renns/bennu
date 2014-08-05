package com.qoid.bennu.integration

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.JsonAssist.jsondsl._
import com.qoid.bennu.client._
import org.specs2.Specification
import org.specs2.execute.Result
import scala.async.Async

class AliasSpec extends Specification {
  implicit val config = HttpClientConfig()

  def is = s2"""
    ${section("integration")}

    Alias should
      create alias          ${createAlias()}
      update alias          ${updateAlias()}
      delete alias          ${deleteAlias()}
      create alias login    ${createAliasLogin()}
      update alias login    ${updateAliasLogin()}
      delete alias login    ${deleteAliasLogin()}
      update alias profile  ${updateAliasProfile()}

    ${section("integration")}
  """

  //TODO: Test error codes coming back from service
  //TODO: Test sending to another agent
  //TODO: Test DoV

  def createAlias(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        Async.await(client.createAlias("Alias", "Profile Name"))
        success
      }
    }.await(60)
  }

  def updateAlias(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val alias = Async.await(client.createAlias("Alias", "Profile Name"))
        val data: JValue = "default" -> true
        val alias2 = Async.await(client.updateAlias(alias.iid, data))
        alias2.data must_== data
      }
    }.await(60)
  }

  def deleteAlias(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val alias = Async.await(client.createAlias("Alias", "Profile Name"))
        val aliasIid = Async.await(client.deleteAlias(alias.iid))
        aliasIid must_== alias.iid
      }
    }.await(60)
  }

  def createAliasLogin(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val aliasName = "Alias"
        val alias = Async.await(client.createAlias(aliasName, "Profile Name"))
        val agent = Async.await(client.getAgent())
        val authenticationId = agent.name.toLowerCase + "." + aliasName.toLowerCase
        val login = Async.await(client.createAliasLogin(alias.iid, "password"))

        login.authenticationId.value must_== authenticationId
      }
    }.await(60)
  }

  def updateAliasLogin(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val alias = Async.await(client.createAlias("Alias", "Profile Name"))
        val login = Async.await(client.createAliasLogin(alias.iid, "password"))
        val login2 = Async.await(client.updateAliasLogin(alias.iid, "password2"))

        login.passwordHash must_!= login2.passwordHash
      }
    }.await(60)
  }

  def deleteAliasLogin(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val alias = Async.await(client.createAlias("Alias", "Profile Name"))
        val login = Async.await(client.createAliasLogin(alias.iid, "password"))
        val aliasIid = Async.await(client.deleteAliasLogin(alias.iid))

        aliasIid must_== alias.iid
      }
    }.await(60)
  }

  def updateAliasProfile(): Result = {
    ClientAssist.channelClient1 { client =>
      Async.async {
        val alias = Async.await(client.createAlias("Alias", "Profile Name"))
        val profileName = "Test Profile"
        val profile = Async.await(client.updateAliasProfile(alias.iid, Some(profileName)))

        profile.name must_== profileName
      }
    }.await(60)
  }
}
