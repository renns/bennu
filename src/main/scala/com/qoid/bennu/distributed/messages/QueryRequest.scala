package com.qoid.bennu.distributed.messages

import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id.Handle
import com.qoid.bennu.model.id.InternalId
import m3.json.Json

object QueryRequest extends FromJsonCapable[QueryRequest]

case class QueryRequest(
  @Json("type") tpe: String,
  query: String,
  historical: Boolean,
  standing: Boolean,
  handle: Handle,
  degreesOfVisibility: Integer,
  connectionIids: List[InternalId]
) extends ToJsonCapable
