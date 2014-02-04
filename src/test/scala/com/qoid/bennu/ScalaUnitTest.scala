package com.qoid.bennu


import org.scalatest.junit.JUnitSuite
import org.junit.Assert._
import net.model3.guice.DependencyInjector
import org.junit.Before


abstract class ScalaUnitTest extends JUnitSuite with org.scalatest.junit.AssertionsForJUnit {

  case class TestData[TInput,TOutput](input: TInput, expectedOutput: TOutput)
  
  @Before def initialize = {
    DependencyInjector.set(new GuiceTestModule().get)
  }

  def runTests[TInput,TOutput](testData: List[TestData[TInput,TOutput]], context: String = "")( code: TInput => TOutput ) = {

    val results = testData.map { td =>
      td.expectedOutput -> code(td.input)
    }

    lazy val heading = println(s"==== ${context} - ${getClass.getName} ====")

    results.foreach {
      case (expected, actual) if actual != expected => {
        heading
        println("    expectedOutput == " + expected)
        println("    actualOutput   == " + actual)
      }
      case _ =>
    }

    results.foreach {
      case (expected, actual) => assertEquals(expected, actual)
      case _ =>
    }

  }

}
