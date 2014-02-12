package com.qoid.bennu

object ServicePath {
  val createAgent = "/api/agent/create"
  val createChannel = "/api/channel/create"
  val pollChannel = "/api/channel/poll"
  val submitChannel = "/api/channel/submit"
  val upsert = "/api/upsert"
  val delete = "/api/delete"
  val query = "/api/query"
  val sendNotification = "/api/notification/send"
  val registerStandingQuery = "/api/squery/register"
  val deRegisterStandingQuery = "/api/squery/deregister"
  val initiateIntroduction = "/api/introduction/initiate"
  val respondToIntroduction = "/api/introduction/respond"
  val getProfiles = "/api/profile/get"
}
