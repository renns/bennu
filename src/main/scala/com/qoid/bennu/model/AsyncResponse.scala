package com.qoid.bennu.model

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable

case class AsyncResponse(
  responseType: AsyncResponseType,
  handle: InternalId,
  data: JValue
) extends ToJsonCapable
