package com.qoid.bennu.model.service

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.ToJsonCapable
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.query.StandingQueryAction
import m3.json.Json

case class QueryResult(
  route: List[InternalId],
  standing: Boolean,
  @Json("type") tpe: String,
  results: List[JValue],
  action: Option[StandingQueryAction]
) extends ToJsonCapable
