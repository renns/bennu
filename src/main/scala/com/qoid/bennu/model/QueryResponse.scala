package com.qoid.bennu.model

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.FromJsonCapable
import com.qoid.bennu.ToJsonCapable
import m3.json.Json
import com.qoid.bennu.squery.StandingQueryAction

object QueryResponse extends FromJsonCapable[QueryResponse]

case class QueryResponse(
  responseType: QueryResponseType,
  handle: Handle,
  @Json("type") tpe: String,
  context: JValue,
  results: JValue,
  aliasIid: Option[InternalId] = None,
  connectionIid: Option[InternalId] = None,
  action: Option[StandingQueryAction] = None
) extends ToJsonCapable
