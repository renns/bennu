package com.qoid.bennu.query

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.query.ast.Evaluator
import com.qoid.bennu.query.ast.Query
import com.qoid.bennu.security.SecurityContext
import m3.predef._

@Singleton
class QueryManager @Inject()(
  injector: ScalaInjector,
  standingQueryRepo: StandingQueryRepository
) {
  def notifyStandingQueries[T : Manifest](
    instance: T,
    action: StandingQueryAction
  ): Unit = {
    val mapper = MapperAssist.findMapperByType[T]
    val agentId = injector.instance[SecurityContext].agentId

    standingQueryRepo.find(agentId, mapper.typeName).foreach { sq =>
      //TODO: set security context
      val securityContext = injector.instance[SecurityContext]

      if (Evaluator.evaluateQuery(securityContext.constrictQuery(mapper, Query.parse(sq.query)), instance) == Evaluator.VTrue) {
        //TODO: send standing query response
      }
    }
  }
}
