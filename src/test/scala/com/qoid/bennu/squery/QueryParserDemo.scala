package com.qoid.bennu.squery
import ast._
import com.qoid.bennu.squery.ast.Node

object QuerryParserDemo extends App {
  
  val parser = new QueryParser
  
  parse(
    original = """xyz()""",
    expected = "xyz()"
  )

  parse(
    original = """xyz() + 35 / 56 * 12 + asdf / 1234 - 1234 + 1234 = sfasfd and qewrqwr > adsfsaf""",
    expected = """xyz() + 35 / 56 * 12 + asdf / 1234 - 1234 + 1234 = sfasfd and qewrqwr > adsfsaf"""
  )

  parse(
    original = """xyz() + 35 / 56 * 12 + (asdf / 1234) - 1234 + 1234 = sfasfd and qewrqwr > adsfsaf"""
  )
  
  
  
  
  def parse(original: String): Unit = parse(original, original)
  
  def parse(original: String, expected: String): Unit = {
    println("=== parsing ===")
    println(s"  ${original}")
    parser.parse(original) match {
      case Left(query) => {
        val actual = Node.reify(query)
        if ( actual == expected ) println("  good")
        else {
          println(s"actual   = ${actual}")
          println(s"expected = ${expected}")
        }
      }
      case result => println("error -> " + result)
    }
  }
  
  
}
