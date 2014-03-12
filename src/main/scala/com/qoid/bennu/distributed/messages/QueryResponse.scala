package com.qoid.bennu.distributed.messages

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.Handle

object QueryResponse extends FromJsonCapable[QueryResponse]

case class QueryResponse(
  handle: Handle,
  results: JValue
) extends ToJsonCapable
