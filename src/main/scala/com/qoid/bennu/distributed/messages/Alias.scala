package com.qoid.bennu.distributed.messages

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Login
import com.qoid.bennu.model.Profile
import com.qoid.bennu.model.id.InternalId

case class CreateAliasRequest(name: String, profileName: String, profileImage: String, data: JValue) extends ToJsonCapable
case class CreateAliasResponse(alias: Alias) extends ToJsonCapable

case class UpdateAliasRequest(aliasIid: InternalId, data: JValue) extends ToJsonCapable
case class UpdateAliasResponse(alias: Alias) extends ToJsonCapable

case class DeleteAliasRequest(aliasIid: InternalId) extends ToJsonCapable
case class DeleteAliasResponse(aliasIid: InternalId) extends ToJsonCapable

case class CreateAliasLoginRequest(aliasIid: InternalId, password: String) extends ToJsonCapable
case class CreateAliasLoginResponse(login: Login) extends ToJsonCapable

case class UpdateAliasLoginRequest(aliasIid: InternalId, password: String) extends ToJsonCapable
case class UpdateAliasLoginResponse(login: Login) extends ToJsonCapable

case class DeleteAliasLoginRequest(aliasIid: InternalId) extends ToJsonCapable
case class DeleteAliasLoginResponse(aliasIid: InternalId) extends ToJsonCapable

case class UpdateAliasProfileRequest(aliasIid: InternalId, profileName: Option[String], profileImage: Option[String]) extends ToJsonCapable
case class UpdateAliasProfileResponse(profile: Profile) extends ToJsonCapable
