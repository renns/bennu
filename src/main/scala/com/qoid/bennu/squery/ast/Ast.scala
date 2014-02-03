package com.qoid.bennu.squery.ast

import com.qoid.bennu.squery.QueryParser
import m3.predef._

object Node {

  def reify(query: Query): String = {
    
    import m3.Chord
    import Chord._
    
    def p(node: Node): Chord = node match {
      case Identifier(p) => p.mkChord(".")
      case StringLit(s) => "\"" ~ s ~ "\""
      case NumericLit(n) => n.toString
      case Parens(e) => "(" ~ p(e) ~ ")"
      case op: Op => p(op.left) ~*~ op.op.symbol ~*~ p(op.right)
      case fn: FunctionCall => fn.name ~ "(" ~ fn.parms.map(p).mkChord(",") ~ ")"
    }
    
    query.expr match {
      case Some(node) => p(node).toString
      case _ => ""
    }    
  }

}

object Query {
  
  val nil = Query(None)
  
  val parser = new QueryParser
  
  def parse(queryStr: String): Query = parser.parse(queryStr) match {
    case Left(q) => q
    case Right(msg) => m3x.error(s"error parsing \n  ${queryStr}\n${msg}")
  }
  
}

case class Query(expr: Option[Node]) {
  def and(that: Option[Node]) = {
    val e = (expr ++ that).toList match {
      case Nil => None
      case List(e0) => Some(e0)
      case List(e0, e1) => Some(BoolOp(e0, e1, operators.and))
    }
    Query(e)
  }
}


sealed trait Node


case class Identifier(parts: List[String]) extends Node
case class StringLit(value: String) extends Node
case class NumericLit(value: BigDecimal) extends Node

case class Parens(expr: Node) extends Node

sealed trait Op {
  val left: Node
  val right: Node
  val op: Operator
}

case class ValueOp(left: Node, right: Node, op: ValueOperator) extends Node with Op
case class BoolOp(left: Node, right: Node, op: BooleanOperator) extends Node with Op

case class FunctionCall(name: String, parms: List[Node]) extends Node

sealed abstract class Operator {
  def symbol: String 
}

case class BooleanOperator(symbol: String) extends Operator
case class ValueOperator(symbol: String) extends Operator 

object operators {
  
  val plus = ValueOperator("+")
  val minus = ValueOperator("-")
  val mult = ValueOperator("*")
  val div = ValueOperator("/")

  val lessThan = BooleanOperator("<")
  val lessThanOrEqual = BooleanOperator("<=")
  val greaterThan = BooleanOperator(">")
  val greaterThanOrEqual = BooleanOperator(">=")
  val equal = BooleanOperator("=")
  val notEqual = BooleanOperator("!=")
  
  val and = BooleanOperator("and")
  val or = BooleanOperator("or")
  
}

