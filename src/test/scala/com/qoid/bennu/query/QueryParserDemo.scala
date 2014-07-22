package com.qoid.bennu.query
import ast._
import com.qoid.bennu.query.ast.Node
import com.qoid.bennu.query.ast.QueryParser

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
    original = """(
    hasLabelPath('RzqX8wdPeq7VnlRxFW2I2eP46emVhOyr','PzsnIXBMtsjMMq5D0evKoezOoBEDwqTQ')
    OR hasLabelPath('RzqX8wdPeq7VnlRxFW2I2eP46emVhOyr','e4Z792gI2sC9WZEaEpR6GxJRX46j7jPj')
)""",
    expected = """(hasLabelPath("RzqX8wdPeq7VnlRxFW2I2eP46emVhOyr","PzsnIXBMtsjMMq5D0evKoezOoBEDwqTQ") or hasLabelPath("RzqX8wdPeq7VnlRxFW2I2eP46emVhOyr","e4Z792gI2sC9WZEaEpR6GxJRX46j7jPj"))"""
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
