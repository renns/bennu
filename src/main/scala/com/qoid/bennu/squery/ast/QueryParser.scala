package com.qoid.bennu.squery.ast

import scala.util.parsing.combinator.JavaTokenParsers

class QueryParser extends JavaTokenParsers {
  
  def parse(queryExpr: String): Either[Query, String] = {
    if (queryExpr.trim.length == 0) Left(Query(None))
    else {
      val actualResult = parseAll(query, queryExpr)
      if (actualResult.successful) Left(Query(Some(actualResult.get)))
      else Right(actualResult.toString)
    }
  }

  
  def query: Parser[Node] = expr
  
  def expr: Parser[Node] = term4
  
  // operator precedence
  def term4 = op(term3, logicOp, BoolOp)
  def term3 = op(term2, boolOp, BoolOp)
  def term2 = op(term1, multOp, ValueOp)
  def term1 = op(term0, addOp, ValueOp)
  
  def term0 = factor
  
  def op[U](termParser: Parser[Node], opParser: Parser[U], constructor: (Node,Node,U) => Node ): Parser[Node] = {
    termParser ~ rep(opParser ~ termParser) ^^ {
      case left ~ rights => {
        rights.foldLeft(left) { case (l, op ~ r) =>
          constructor(l, r, op)
        }
      }
    }    
  }
  
  def inClause = identifier ~ k("in") ~ "(" ~ repsep(literal, ",") ~ ")" ^^ { case expr ~ _ ~ _ ~ values ~ _ => InClause(expr, values) }
  
  def factor: Parser[Node] = inClause | functionCall | identifier | stringLit | numericLit | parens 
  
  def functionCall = ident ~ "(" ~ repsep(expr, ",") ~ ")" ^^ {
    case name ~ _ ~ parms ~ _ => FunctionCall(name, parms)
  }
  
  def parens = ("(" ~> expr ^^ Parens) <~ ")" 
  
  def identifier = ident ~ rep("." ~> ident) ^^ { case l ~ r => Identifier(l::r) }
  
  def literal = nullLit | stringLit | numericLit
  
  def nullLit = k("null") ^^ (_=>NullLit)
  
  def stringLit = stringLiteral ^^ StringLit
  
  def numericLit = sign ~ unsignedDecimal ^^ { case s ~ num => NumericLit(s * num) }
  
  def unsignedDecimal = decimalNumber ^^ (d=>BigDecimal(d))
  
  def sign = opt(k("-")) ^^ { case s => if ( s.isDefined ) -1 else +1 }

  def multOp = (
    "*" ^^ (_=>operators.mult) 
    | "/" ^^ (_=>operators.div)
  )

  def addOp = (
    "+" ^^ (_=>operators.plus) 
    | "-" ^^ (_=>operators.minus)
  )

  def logicOp = (
    k("and") ^^ (_=>operators.and) 
    | k("or") ^^ (_=>operators.or)
  )

  def boolOp = (
      ("<>" | "!=") ^^ (_=>operators.notEqual) 
      | ">=" ^^ (_=>operators.greaterThanOrEqual)
      | ">"  ^^ (_=>operators.greaterThan)
      | "<=" ^^ (_=>operators.lessThanOrEqual)
      | "<"  ^^ (_=>operators.lessThan) 
      | "="  ^^ (_=>operators.equal)
  )

  override def stringLiteral = (
    super.stringLiteral
    | ("\'"+"""([^'\p{Cntrl}\\]|\\[\\/bfnrt]|\\u[a-fA-F0-9]{4})*"""+"\'").r
  ) ^^ (s => s.substring(1, s.length()-1)) // strip quotes

  /** A parser that matches a case insensitive keyword string */
  def k(s: String): Parser[String] = new Parser[String] {
    def apply(in: Input) = {
      val source = in.source
      val offset = in.offset
      val start = handleWhiteSpace(source, offset)
      var i = 0
      var j = start
      while (i < s.length && j < source.length && Character.toUpperCase(s.charAt(i)) == Character.toUpperCase((source.charAt(j)))) {
        i += 1
        j += 1
      }
      if (i == s.length && (j == source.length() || !Character.isJavaIdentifierPart(source.charAt(j))))
        Success(source.subSequence(start, j).toString, in.drop(j - offset))
      else {
        val found = if (start == source.length()) "end of source" else "`" + source.charAt(start) + "'"
        Failure("`" + s + "' expected but " + found + " found", in.drop(start - offset))
      }
    }
  }

}

