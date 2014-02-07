package com.qoid.bennu.squery.ast

import com.qoid.bennu.ScalaUnitTest
import org.junit.Test
import m3.Chord
import com.qoid.bennu.model.AgentId

class EvaluatorTest extends ScalaUnitTest { 

  import Evaluator._
  
  case class TestRow(xyz: String, agentId: AgentId = AgentId(""))

  @Test def basic = {
    
    runTests[(String,TestRow),Value](
      List(
        TestData(
            "xyz = '1'" -> TestRow("1"),
            VTrue
        ),
        TestData(
            "xyz = '1'" -> TestRow("2"),
            VFalse
        )
      )
    ) { input =>
      val query = Query.parse(input._1)
      evaluateQuery(query, input._2)
    }
  
  }
  
  @Test def inClause = {
    
    runTests[(String,TestRow),Value](
      List(
        TestData(
            "xyz in ('1')" -> TestRow("1"),
            VTrue
        ),
        TestData(
            "xyz in ('1')" -> TestRow("2"),
            VFalse
        )
      )
    ) { input =>
      val query = Query.parse(input._1)
      evaluateQuery(query, input._2)
    }
  
  }

  @Test def agentIdComparison = {
    
    runTests[(String,TestRow),Value](
      List(
        TestData(
            "agentId = '1'" -> TestRow("agent id 1", AgentId("1")),
            VTrue
        ),
        TestData(
            "agentId = '1'" -> TestRow("agent id 2", AgentId("2")),
            VFalse
        )
      )
    ) { input =>
      val query = Query.parse(input._1)
      evaluateQuery(query, input._2)
    }
  
  }
  
  
}