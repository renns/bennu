package com.qoid.bennu


import org.scalatest.junit.JUnitSuite
import org.junit.Assert._
import net.model3.guice.DependencyInjector
import org.junit.Before


abstract class ScalaUnitTest extends JUnitSuite with org.scalatest.junit.AssertionsForJUnit {

  @Before def initialize = {
    DependencyInjector.set(new GuiceTestModule().get)
  }
  
  def runTests[TInput,TOutput](tests: List[(TInput,TOutput)], context: String = "")( code: TInput => TOutput ) = {

    val results = tests.map { case (input, expectedOutput) =>
      expectedOutput -> code(input)
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
