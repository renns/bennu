package com.qoid.bennu.squery.ast

import m3.predef._
import net.model3.chrono.{DateFormatter, DateTime}

object Node {

  def reify(query: Query): String = {
    
    import m3.Chord
    import Chord._
    
    def p(node: Node): Chord = node match {
      case InClause(e, v) => p(e) ~*~ "in" ~*~ "(" ~ v.map(p).mkChord(",") ~ ")"
      case Identifier(p) => p.mkChord(".")
      case StringLit(s) => "\"" ~ s ~ "\""
      case NullLit => "null"
      case NumericLit(n) => n.toString()
      case BooleanLit(b) => b.toString
      case Parens(e) => "(" ~ p(e) ~ ")"
      case op: Op => p(op.left) ~*~ op.op.symbol ~*~ p(op.right)
      case fn: FunctionCall => fn.name ~ "(" ~ fn.parms.map(p).mkChord(",") ~ ")"
    }
    
    query.expr match {
      case Some(node) => p(node).toString()
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
  def and(that: Query): Query = and(that.expr)
}


sealed trait Node


case class Identifier(parts: List[String]) extends Node {
  val qname = parts.mkString(".")
}

sealed trait Literal extends Node {
  def value: Any
}

case class StringLit(value: String) extends Literal
case class NumericLit(value: BigDecimal) extends Literal
case class BooleanLit(value: Boolean) extends Literal
case object NullLit extends Literal {
  def value = null
}

case class Parens(expr: Node) extends Node

sealed trait Op extends Node {
  val left: Node
  val right: Node
  val op: Operator
}

case class ValueOp(left: Node, right: Node, op: ValueOperator) extends Node with Op
case class BoolOp(left: Node, right: Node, op: BooleanOperator) extends Node with Op

case class InClause(left: Identifier, values: List[Literal]) extends Node

case class FunctionCall(name: String, parms: List[Node]) extends Node

sealed abstract class Operator {
  def symbol: String
  val evaluator: PartialFunction[(Evaluator.Value,Evaluator.Value), Evaluator.Value]
}

case class BooleanOperator(symbol: String)(val evaluator: PartialFunction[(Evaluator.Value,Evaluator.Value), Evaluator.Value]) extends Operator
case class ValueOperator(symbol: String)(val evaluator: PartialFunction[(Evaluator.Value,Evaluator.Value), Evaluator.Value]) extends Operator 

object operators {
  
  import Evaluator._
  
  val plus = ValueOperator("+") {
    case (VNum(l), VNum(r)) => VNum(l+r)
  }
  
  val minus = ValueOperator("-") {
    case (VNum(l), VNum(r)) => VNum(l-r)
  }
  
  val mult = ValueOperator("*") {
    case (VNum(l), VNum(r)) => VNum(l*r)
  }
  
  val div = ValueOperator("/") {
    case (VNum(l), VNum(r)) => VNum(l/r)
  }

  val lessThan = BooleanOperator("<") {
    case (VNum(l), VNum(r)) => VBool(l < r)
    case (VStr(l), VStr(r)) => VBool(l < r)
    case (VDate(l), VStr(r)) => VBool(l < parseDateTime(r))
  }
  
  val lessThanOrEqual = BooleanOperator("<=") {
    case (VNum(l), VNum(r)) => VBool(l <= r)
    case (VStr(l), VStr(r)) => VBool(l <= r)
    case (VDate(l), VStr(r)) => VBool(l <= parseDateTime(r))
  }
  
  val greaterThan = BooleanOperator(">") {
    case (VNum(l), VNum(r)) => VBool(l > r)
    case (VStr(l), VStr(r)) => VBool(l > r)
    case (VDate(l), VStr(r)) => VBool(l > parseDateTime(r))
  }
  
  val greaterThanOrEqual = BooleanOperator(">=") {
    case (VNum(l), VNum(r)) => VBool(l >= r)
    case (VStr(l), VStr(r)) => VBool(l >= r)
    case (VDate(l), VStr(r)) => VBool(l >= parseDateTime(r))
  }
  
  val equal = BooleanOperator("=") {
    case (VNum(l), VNum(r)) => VBool(l == r)
    case (VBool(l), VBool(r)) => VBool(l == r)
    case (VStr(l), VStr(r)) => VBool(l == r)
    case (VDate(l), VStr(r)) => VBool(l == parseDateTime(r))
  }
  
  val notEqual = BooleanOperator("<>") {
    case (VNum(l), VNum(r)) => VBool(l != r)
    case (VBool(l), VBool(r)) => VBool(l != r)
    case (VStr(l), VStr(r)) => VBool(l != r)
    case (VDate(l), VStr(r)) => VBool(l != parseDateTime(r))
  }
  
  val and = BooleanOperator("and") {
    case (VBool(l), VBool(r)) => VBool(l && r)
  }
  
  val or = BooleanOperator("or") {
    case (VBool(l), VBool(r)) => VBool(l || r)
  }

  private def parseDateTime(dateString: String): DateTime = new DateFormatter("yyyy-MM-dd HH:mm:ss.SSS").parse(dateString)
}

