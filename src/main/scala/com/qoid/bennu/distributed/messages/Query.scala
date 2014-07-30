package com.qoid.bennu.distributed.messages

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.query.StandingQueryAction
import m3.json.Json

case class QueryRequest(
  @Json("type") tpe: String,
  query: String,
  historical: Boolean,
  standing: Boolean
) extends ToJsonCapable

case class QueryResponse(
  @Json("type") tpe: String,
  results: List[JValue]
) extends ToJsonCapable

case class StandingQueryResponse(
  @Json("type") tpe: String,
  result: JValue,
  action: StandingQueryAction
) extends ToJsonCapable
