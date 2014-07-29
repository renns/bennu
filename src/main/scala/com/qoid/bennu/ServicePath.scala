package com.qoid.bennu

object ServicePath {
  // Agent
  val createAgent = "/api/v1/agent/create"
//  val deleteAgent = "/api/v1/agent/delete"
  val importAgent = "/api/v1/agent/import"

  // Session
  val login = "/api/v1/login"
  val logout = "/api/v1/logout"

  // Channel
  val pollChannel = "/api/v1/channel/poll"
  val submitChannelRequests = "/api/v1/channel/submit"

  // Query
  val query = "/api/v1/query"
//  val cancelQuery = "/api/v1/query/cancel"

  // Alias

  // Connection

  // Content
  val createContent = "/api/v1/content/create"

  // Label
  val createLabel = "/api/v1/label/create"

  // Notification

  // Introduction
//  val initiateIntroduction = "/api/v1/introduction/initiate"
//  val respondToIntroduction = "/api/v1/introduction/respond"

  // Verification
//  val requestVerification = "/api/v1/verification/request"
//  val respondToVerification = "/api/v1/verification/respond"
//  val verify = "/api/verification/v1/verify"
//  val acceptVerification = "/api/v1/verification/accept"
}
