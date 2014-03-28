package com.qoid.bennu

object ServicePath {
  val createAgent = "/api/agent/create"
  val createChannel = "/api/channel/create"
  val pollChannel = "/api/channel/poll"
  val submitChannel = "/api/channel/submit"
  val upsert = "/api/upsert"
  val delete = "/api/delete"
  val query = "/api/query"
  val deRegisterStandingQuery = "/api/query/deregister"
  val initiateIntroduction = "/api/introduction/initiate"
  val respondToIntroduction = "/api/introduction/respond"
  val requestVerification = "/api/verification/request"
  val respondToVerification = "/api/verification/respond"
  val verify = "/api/verification/verify"
  val acceptVerification = "/api/verification/accept"
}
