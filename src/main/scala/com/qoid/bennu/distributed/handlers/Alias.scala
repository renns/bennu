package com.qoid.bennu.distributed.handlers

import com.qoid.bennu.BennuException
import com.qoid.bennu.ErrorCode
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.DistributedRequestHandler
import com.qoid.bennu.distributed.DistributedResponseHandler
import com.qoid.bennu.distributed.messages
import com.qoid.bennu.model.{Profile, Alias}
import com.qoid.bennu.model.assist.AliasAssist
import com.qoid.bennu.security.{AuthenticationManager, SecurityContext}
import m3.jdbc._
import m3.predef._

object CreateAliasRequest extends DistributedRequestHandler[messages.CreateAliasRequest] with Logging {
  override protected val requestKind = DistributedMessageKind.CreateAliasRequest
  override protected val responseKind = DistributedMessageKind.CreateAliasResponse
  override protected val allowedVersions = List(1)

  override protected def validateRequest(request: messages.CreateAliasRequest): Unit = {
    if (request.name.isEmpty) throw new BennuException(ErrorCode.nameInvalid)
    if (request.profileName.isEmpty) throw new BennuException(ErrorCode.profileNameInvalid)
  }

  override def process(request: messages.CreateAliasRequest, injector: ScalaInjector): JValue = {
    val aliasAssist = injector.instance[AliasAssist]
    val securityContext = injector.instance[SecurityContext]

    val alias = aliasAssist.createAlias(request.name, request.profileName, request.profileImage, Some(securityContext.aliasIid))

    messages.CreateAliasResponse(alias).toJson
  }
}

object CreateAliasResponse extends DistributedResponseHandler[messages.CreateAliasResponse] with Logging {
  override protected val responseKind = DistributedMessageKind.CreateAliasResponse
  override protected val allowedVersions = List(1)
}

object UpdateAliasRequest extends DistributedRequestHandler[messages.UpdateAliasRequest] with Logging {
  override protected val requestKind = DistributedMessageKind.UpdateAliasRequest
  override protected val responseKind = DistributedMessageKind.UpdateAliasResponse
  override protected val allowedVersions = List(1)

  override def process(request: messages.UpdateAliasRequest, injector: ScalaInjector): JValue = {
    val alias = Alias.fetch(request.aliasIid)
    val alias2 = Alias.update(alias.copy(data = request.data))
    messages.UpdateAliasResponse(alias2).toJson
  }
}

object UpdateAliasResponse extends DistributedResponseHandler[messages.UpdateAliasResponse] with Logging {
  override protected val responseKind = DistributedMessageKind.UpdateAliasResponse
  override protected val allowedVersions = List(1)
}

object DeleteAliasRequest extends DistributedRequestHandler[messages.DeleteAliasRequest] with Logging {
  override protected val requestKind = DistributedMessageKind.DeleteAliasRequest
  override protected val responseKind = DistributedMessageKind.DeleteAliasResponse
  override protected val allowedVersions = List(1)

  override def process(request: messages.DeleteAliasRequest, injector: ScalaInjector): JValue = {
    val aliasAssist = injector.instance[AliasAssist]
    aliasAssist.deleteAlias(request.aliasIid)
    messages.DeleteAliasResponse(request.aliasIid).toJson
  }
}

object DeleteAliasResponse extends DistributedResponseHandler[messages.DeleteAliasResponse] with Logging {
  override protected val responseKind = DistributedMessageKind.DeleteAliasResponse
  override protected val allowedVersions = List(1)
}

object CreateAliasLoginRequest extends DistributedRequestHandler[messages.CreateAliasLoginRequest] with Logging {
  override protected val requestKind = DistributedMessageKind.CreateAliasLoginRequest
  override protected val responseKind = DistributedMessageKind.CreateAliasLoginResponse
  override protected val allowedVersions = List(1)

  override def process(request: messages.CreateAliasLoginRequest, injector: ScalaInjector): JValue = {
    val authenticationMgr = injector.instance[AuthenticationManager]
    val login = authenticationMgr.createLogin(request.aliasIid, request.password)
    messages.CreateAliasLoginResponse(login).toJson
  }
}

object CreateAliasLoginResponse extends DistributedResponseHandler[messages.CreateAliasLoginResponse] with Logging {
  override protected val responseKind = DistributedMessageKind.CreateAliasLoginResponse
  override protected val allowedVersions = List(1)
}

object UpdateAliasLoginRequest extends DistributedRequestHandler[messages.UpdateAliasLoginRequest] with Logging {
  override protected val requestKind = DistributedMessageKind.UpdateAliasLoginRequest
  override protected val responseKind = DistributedMessageKind.UpdateAliasLoginResponse
  override protected val allowedVersions = List(1)

  override def process(request: messages.UpdateAliasLoginRequest, injector: ScalaInjector): JValue = {
    val authenticationMgr = injector.instance[AuthenticationManager]
    val login = authenticationMgr.updatePassword(request.aliasIid, request.password)
    messages.UpdateAliasLoginResponse(login).toJson
  }
}

object UpdateAliasLoginResponse extends DistributedResponseHandler[messages.UpdateAliasLoginResponse] with Logging {
  override protected val responseKind = DistributedMessageKind.UpdateAliasLoginResponse
  override protected val allowedVersions = List(1)
}

object DeleteAliasLoginRequest extends DistributedRequestHandler[messages.DeleteAliasLoginRequest] with Logging {
  override protected val requestKind = DistributedMessageKind.DeleteAliasLoginRequest
  override protected val responseKind = DistributedMessageKind.DeleteAliasLoginResponse
  override protected val allowedVersions = List(1)

  override def process(request: messages.DeleteAliasLoginRequest, injector: ScalaInjector): JValue = {
    val authenticationMgr = injector.instance[AuthenticationManager]
    authenticationMgr.deleteLogin(request.aliasIid)
    messages.DeleteAliasLoginResponse(request.aliasIid).toJson
  }
}

object DeleteAliasLoginResponse extends DistributedResponseHandler[messages.DeleteAliasLoginResponse] with Logging {
  override protected val responseKind = DistributedMessageKind.DeleteAliasLoginResponse
  override protected val allowedVersions = List(1)
}

object UpdateAliasProfileRequest extends DistributedRequestHandler[messages.UpdateAliasProfileRequest] with Logging {
  override protected val requestKind = DistributedMessageKind.UpdateAliasProfileRequest
  override protected val responseKind = DistributedMessageKind.UpdateAliasProfileResponse
  override protected val allowedVersions = List(1)

  override def process(request: messages.UpdateAliasProfileRequest, injector: ScalaInjector): JValue = {
    val profile = Profile.selectOne(sql"aliasIid = ${request.aliasIid}")

    val profile2 = (request.profileName, request.profileImage) match {
      case (Some(profileName), Some(profileImage)) => profile.copy(name = profileName, imgSrc = profileImage)
      case (Some(profileName), None) => profile.copy(name = profileName)
      case (None, Some(profileImage)) => profile.copy(imgSrc = profileImage)
      case _ => throw new BennuException(ErrorCode.profileNameProfileImageInvalid)
    }

    val profile3 = Profile.update(profile2)

    messages.UpdateAliasProfileResponse(profile3).toJson
  }
}

object UpdateAliasProfileResponse extends DistributedResponseHandler[messages.UpdateAliasProfileResponse] with Logging {
  override protected val responseKind = DistributedMessageKind.UpdateAliasProfileResponse
  override protected val allowedVersions = List(1)
}

