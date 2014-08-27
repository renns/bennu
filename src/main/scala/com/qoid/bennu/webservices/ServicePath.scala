package com.qoid.bennu.webservices

object ServicePath {
  // Agent
  val createAgent = "/api/v1/agent/create"
//  val deleteAgent = "/api/v1/agent/delete"
  val importAgent = "/api/v1/agent/import"

  // Session
  val login = "/api/v1/login"
  val logout = "/api/v1/logout"
  val spawnSession = "/api/v1/session/spawn"

  // Channel
  val pollChannel = "/api/v1/channel/poll"
  val submitChannelRequests = "/api/v1/channel/submit"

  // Query
  val query = "/api/v1/query"
  val cancelQuery = "/api/v1/query/cancel"

  // Alias
  val createAlias = "/api/v1/alias/create"
  val updateAlias = "/api/v1/alias/update"
  val deleteAlias = "/api/v1/alias/delete"
  val createAliasLogin = "/api/v1/alias/login/create"
  val updateAliasLogin = "/api/v1/alias/login/update"
  val deleteAliasLogin = "/api/v1/alias/login/delete"
  val updateAliasProfile = "/api/v1/alias/profile/update"

  // Connection
  val deleteConnection = "/api/v1/connection/delete"

  // Content
  val createContent = "/api/v1/content/create"
  val updateContent = "/api/v1/content/update"
  val addContentLabel = "/api/v1/content/label/add"
  val removeContentLabel = "/api/v1/content/label/remove"

  // Label
  val createLabel = "/api/v1/label/create"
  val updateLabel = "/api/v1/label/update"
  val moveLabel = "/api/v1/label/move"
  val copyLabel = "/api/v1/label/copy"
  val removeLabel = "/api/v1/label/remove"
  val grantLabelAccess = "/api/v1/label/access/grant"
  val revokeLabelAccess = "/api/v1/label/access/revoke"
  val updateLabelAccess = "/api/v1/label/access/update"

  // Notification
  val createNotification = "/api/v1/notification/create"
  val consumeNotification = "/api/v1/notification/consume"
  val deleteNotification = "/api/v1/notification/delete"

  // Introduction
  val initiateIntroduction = "/api/v1/introduction/initiate"
  val acceptIntroduction = "/api/v1/introduction/accept"

  // Verification
//  val requestVerification = "/api/v1/verification/request"
//  val respondToVerification = "/api/v1/verification/respond"
//  val verify = "/api/verification/v1/verify"
//  val acceptVerification = "/api/v1/verification/accept"
}
