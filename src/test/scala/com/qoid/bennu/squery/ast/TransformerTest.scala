package com.qoid.bennu.squery.ast

import com.qoid.bennu.ScalaUnitTest
import org.junit.Test
import m3.Chord

class TransformerTest extends ScalaUnitTest { 

  @Test def basic = {
    
    runTests[String,String](
      List(
        TestData(
            "xyz", 
            "xyz"
        ),
        TestData(
            "xyz(1)", 
            "xyz(1)"
        )
      )
    ) { input =>
      doTransform(input)()
    }
  
  }

  def doTransform(input: String)(xform: PartialFunction[Node,Chord] = PartialFunction.empty): String = {
    val query = Query.parse(input)
    Transformer.queryToSql(query, xform).toString
  }

  @Test def basicTransform = {
    
    runTests[String,String](
      List(
        TestData(
            "xyz", 
            "booyaka.xyz"
        ),
        TestData(
            "xyz(1)", 
            "xyz(1)"
        )
      )
    ) { input =>
      doTransform(input) { 
        case i: Identifier => "booyaka" + "." + i.parts.mkString(".")
      }
    }
  
  }
  
}