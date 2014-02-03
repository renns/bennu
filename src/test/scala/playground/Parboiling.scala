package playground


import scala.annotation.tailrec
import scala.util.{Failure, Success}
import org.parboiled2._


object Parboiling extends App {

  Calculator2.repl()
  
  object Calculator2 extends App {
  
    @tailrec
    def repl(): Unit = {
      // TODO: Replace next three lines with `scala.Predef.readLine(text: String, args: Any*)`
      // once BUG https://issues.scala-lang.org/browse/SI-8167 is fixed
      print("---\nEnter calculator expression > ")
      Console.out.flush()
      readLine() match {
        case "" =>
        case line =>
          val parser = new Calculator2(line)
          parser.InputLine.run() match {
            case Success(exprAst)       => println("Result: " + eval(exprAst))
            case Failure(e: ParseError) => println("Expression is not valid: " + parser.formatError(e))
            case Failure(e)             => println("Unexpected error during parsing run: " + e)
          }
          repl()
      }
    }
  
    def eval(expr: Expr): Int =
      expr match {
        case Value(v)             => v.toInt
        case Addition(a, b)       => eval(a) + eval(b)
        case Subtraction(a, b)    => eval(a) - eval(b)
        case Multiplication(a, b) => eval(a) * eval(b)
        case Division(a, b)       => eval(a) / eval(b)
        case Parens(e)            => eval(e)
      }
    
    // our abstract syntax tree model
    sealed trait Expr
    case class Value(value: String) extends Expr
    case class Addition(lhs: Expr, rhs: Expr) extends Expr
    case class Subtraction(lhs: Expr, rhs: Expr) extends Expr
    case class Multiplication(lhs: Expr, rhs: Expr) extends Expr
    case class Division(lhs: Expr, rhs: Expr) extends Expr
    case class Parens(e: Expr) extends Expr
  }
  
  /**
   * This parser reads simple calculator expressions and builds an AST
   * for them, to be evaluated in a separate phase, after parsing is completed.
   */
  class Calculator2(val input: ParserInput) extends Parser {
    import Calculator2._
    
    def InputLine = rule { expression ~ EOI }
  
    def expression: Rule1[Expr] = rule {
      term ~ zeroOrMore(
        '+' ~ term ~> Addition
      | '-' ~ term ~> Subtraction)
    }
  
    def term = rule {
      factor ~ zeroOrMore(
        '*' ~ factor ~> Multiplication
      | '/' ~ factor ~> Division)
    }
  
    def factor = rule { number | parens }
  
    def parens = rule { '(' ~ expression ~ ')' ~> Parens }
  
    def number = rule { capture(digits) ~> Value }
  
    def digits = rule { oneOrMore(CharPredicate.Digit) }
  }
}




