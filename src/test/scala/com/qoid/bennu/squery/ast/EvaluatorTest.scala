package com.qoid.bennu.squery.ast

import com.qoid.bennu.ScalaUnitTest
import org.junit.Test
import m3.Chord

class EvaluatorTest extends ScalaUnitTest { 

  import Evaluator._
  
  case class TestRow(xyz: String)
  
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

  
}