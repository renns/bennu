package com.qoid.bennu.query.ast

import m3.Chord
import Chord._
import scala.language.implicitConversions

object Transformer {

  def queryToSql(query: Query, transformer: PartialFunction[Node,Chord] = PartialFunction.empty): Chord = {
    def p(n: Node): Chord = {
      transformer.applyOrElse(n, { n: Node => transformNodeToSql(n)(p) })
    }
    query.expr.map(p).getOrElse("")
  }

  def simpleNodeToSql(n: Node): Chord = transformNodeToSql(n)(simpleNodeToSql)

  def transformNodeToSql(n: Node)(transformer: Node=>Chord): Chord = n match {
    case InClause(e, List()) => transformer(e) ~*~ "in" ~*~ "(null)"
    case InClause(e, v) => transformer(e) ~*~ "in" ~*~ "(" ~ v.map(transformer).mkChord(",") ~ ")"
    case i: Identifier => i.parts.mkString(".")
    case fc: FunctionCall => fc.name ~ "(" ~ fc.parms.map(transformer).mkChord(", ") ~ ")"
    case op: Op => transformer(op.left) ~*~ op.op.symbol ~*~ transformer(op.right)
    case NumericLit(value) => value.toString
    case BooleanLit(b) => b.toString
    case Parens(e) => "(" ~ transformer(e) ~ ")" 
    case StringLit(value) => "'" ~ value ~ "'"
    case NullLit => "null"
  }

  def reify(n: Node): Chord = simpleNodeToSql(n)

}

trait Transformer {
  
}
