package com.qoid.bennu.distributed.messages

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id.Handle
import com.qoid.bennu.squery.StandingQueryAction

object QueryResponse extends FromJsonCapable[QueryResponse]

case class QueryResponse(
  handle: Handle,
  results: JValue,
  standing: Boolean = false,
  action: Option[StandingQueryAction] = None
) extends ToJsonCapable
