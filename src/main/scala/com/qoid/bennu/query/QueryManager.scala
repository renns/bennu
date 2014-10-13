package com.qoid.bennu.query

import com.google.inject.Inject
import com.google.inject.Singleton
import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.distributed.DistributedManager
import com.qoid.bennu.distributed.DistributedMessage
import com.qoid.bennu.distributed.DistributedMessageKind
import com.qoid.bennu.distributed.messages
import com.qoid.bennu.mapper.MapperAssist
import com.qoid.bennu.query.ast.Evaluator
import com.qoid.bennu.query.ast.Query
import com.qoid.bennu.security.ConnectionSecurityContext
import com.qoid.bennu.security.SecurityContext
import m3.predef._
import net.model3.transaction.Transaction

@Singleton
class QueryManager @Inject()(
  injector: ScalaInjector,
  standingQueryRepo: StandingQueryRepository,
  distributedMgr: DistributedManager
) extends Logging {

  def notifyStandingQueries[T : Manifest](instance: T, action: StandingQueryAction): Unit = {
    val txn = injector.instance[Transaction]

    txn.events().addListener(new Transaction.Adapter {
      override def commit(t: Transaction): Unit = {
        evaluateStandingQueries(instance, action)
      }
    })
  }

  private def evaluateStandingQueries[T : Manifest](instance: T, action: StandingQueryAction): Unit = {
    val mapper = MapperAssist.findMapperByType[T]
    val agentId = injector.instance[SecurityContext].agentId

    standingQueryRepo.find(agentId, mapper.typeName).foreach { sq =>

      ConnectionSecurityContext(sq.replyRoute.head, sq.replyRoute.size, injector) {
        val securityContext = injector.instance[SecurityContext]

        val query = securityContext.constrictQuery(mapper, Query.parse(sq.query))

        logger.debug("Evaluating query: " + ast.Node.reify(query))

        if (Evaluator.evaluateQuery(query, instance) == Evaluator.VTrue) {
          val result = serializer.toJson(instance)
          val response = messages.StandingQueryResponse(sq.tpe, result, action)

          val responseMessage = DistributedMessage(
            DistributedMessageKind.StandingQueryResponse,
            1,
            sq.replyRoute,
            response.toJson,
            Some(sq.messageId)
          )

          distributedMgr.send(responseMessage)
        }
      }
    }
  }
}
