package com.qoid.bennu.client

import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.id.InternalId
import m3.servlet.longpoll.ChannelId

case class AliasIid(aliasIid: InternalId)
case class ConnectionIid(connectionIid: InternalId)
case class LabelIid(labelIid: InternalId)
case class NotificationIid(notificationIid: InternalId)
case class IntroductionIid(introductionIid: InternalId)
case class Session(channelId: ChannelId, alias: Alias)
case class ContentLabel(contentIid: InternalId, labelIid: InternalId)
