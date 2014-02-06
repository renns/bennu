package com.qoid.bennu.squery.ast


import m3.predef._
import m3.StringConverters
import m3.TypeInfo

object Evaluator {

  val stringConverters = inject[StringConverters]
  
  import Transformer._
  
  sealed trait Value {
    def value: Any
  }
  
  case object VNull extends Value {
    def value = null
  }
  case class VNum(value: BigDecimal) extends Value
  case class VStr(value: String) extends Value
  case class VBool(value: Boolean) extends Value
  
  val VFalse = VBool(false)
  val VTrue = VBool(true)
  
  
  def evaluateQuery(query: Query, row: Any)(implicit propertyGetter: (Any,String) => Value = simplePropertyGetter): Value = 
    query.expr.map(e=>evaluateNode(e, row)).getOrElse(VTrue)
  
  def evaluateNode(n: Node, row: Any)(implicit propertyGetter: (Any,String) => Value = simplePropertyGetter): Value = n match {
    case InClause(i, v) => {
      val columnValue = propertyGetter(row, i.qname).value
      VBool(v.find(_.value == columnValue).isDefined)
    }
    case i: Identifier => propertyGetter(row, i.qname)
    case fc: FunctionCall => m3x.error("we don't support any function calls yet.  Let alone the one you want -- " + reify(fc))
    case op: Op => {
      val l = evaluateNode(op.left, row)
      val r = evaluateNode(op.right, row)
      op.op.evaluator(l, r)
    }
    case NumericLit(value) => VNum(value)
    case Parens(e) => evaluateNode(e, row)
    case StringLit(value) => VStr(value)
  }


  def simplePropertyGetter(a: Any, propertyName: String): Value = {
    a.getClass.getMethods.find{ m =>
      m.getName =:= propertyName && m.getParameterTypes.isEmpty
    }.map { m =>
      m.setAccessible(true)
      def wrap(a: Any): Value = a match {
        case null => VNull
        case None => VNull
        case Some(a) => wrap(a)
        case bd: BigDecimal => VNum(bd)
        case s: String => VStr(s)
        case a: Any => {
          val converter = stringConverters.find(TypeInfo(a.getClass)).asInstanceOf[StringConverters.Converter[Any]]
          VStr(converter.toString(a))
        }
      }
      wrap(m.invoke(a))
    }.getOrError(s"cannot find property ${propertyName} in ${a.getClass()} -- ${a}")
  }
  
}