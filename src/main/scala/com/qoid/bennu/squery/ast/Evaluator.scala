package com.qoid.bennu.squery.ast

import com.qoid.bennu.Enum
import com.qoid.bennu.model.Content
import com.qoid.bennu.model.LabeledContent
import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.SecurityContext
import java.sql.{ Connection => JdbcConn }
import m3.StringConverters
import m3.StringConverters.Converter
import m3.TypeInfo
import m3.predef._
import m3.predef.box._
import net.model3.chrono.DateTime

object Evaluator extends Logging {

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
  case class VDate(value: DateTime) extends Value
  
  val VFalse = VBool(false)
  val VTrue = VBool(true)
  
  def invokeFunction(fc: FunctionCall, row: Any)(implicit propertyGetter: (Any,String) => Value = simplePropertyGetter) = {
    implicit val jdbcConn = inject[JdbcConn]
    implicit val agentView = inject[AgentView]    

    fc.name match {
      case "hasLabelPath" =>
        val path = ContentQuery.stringLiterals(fc.parms)
        agentView.resolveLabel(path) match {
          case Full(l) => impl.hasLabel(row, l.iid, true)
          case _ => VFalse
        }
      case "hasLabel" => impl.hasLabel(row, InternalId(ContentQuery.stringLiteral(fc.parms)), true)
      case "hasConnectionMetaLabel" =>
        agentView.resolveConnectionMetaLabel() match {
          case Full(iid) => impl.hasLabel(row, iid, false)
          case _ => VFalse
        }
      case _ => m3x.error("function call not supported -- " + reify(fc))
    }
  }
  
  object impl {
    def hasLabel(row: Any, labelIid: InternalId, recursive: Boolean)(implicit av: AgentView, jdbcConn: JdbcConn): VBool = {
      row match {
        case c: Content =>
          val labelIids = if (recursive) {
            SecurityContext.resolveLabelAncestry(labelIid).toList
          } else {
            List(labelIid)
          }

          val labeledContent = av.select[LabeledContent]("labelIid in (" + labelIids.map("'" + _.value + "'").mkString(",") + ")")

          if (labeledContent.exists(_.contentIid == c.iid)) VTrue else VFalse
        case _ => VFalse
      }
    }
  }
  
  def evaluateQuery(query: Query, row: Any)(implicit propertyGetter: (Any,String) => Value = simplePropertyGetter): Value = 
    query.expr.map(e=>evaluateNode(e, row)).getOrElse(VTrue)
  
  def evaluateNode(n: Node, row: Any)(implicit propertyGetter: (Any,String) => Value = simplePropertyGetter): Value = n match {
    case InClause(i, v) => {
      val columnValue = propertyGetter(row, i.qname).value
      VBool(v.exists(_.value == columnValue))
    }
    case i: Identifier => propertyGetter(row, i.qname)
    case fc: FunctionCall => invokeFunction(fc, row)
    case op: Op => {
      val l = evaluateNode(op.left, row)
      val r = evaluateNode(op.right, row)
      op.op.evaluator(l, r)
    }
    case NumericLit(value) => VNum(value)
    case BooleanLit(value) => VBool(value)
    case Parens(e) => evaluateNode(e, row)
    case StringLit(value) => VStr(value)
    case NullLit => VNull
  }

  def simplePropertyGetter(a: Any, propertyName: String): Value = {
    a.getClass.getMethods.find{ m =>
      m.getName =:= propertyName && m.getParameterTypes.isEmpty
    }.map { m =>
      m.setAccessible(true)
      def wrap(a: Any): Value = a match {
        case null => VNull
        case None => VNull
        case Some(x) => wrap(x)
        case bd: BigDecimal => VNum(bd)
        case i: Int => VNum(i)
        case s: String => VStr(s)
        case b: Boolean => VBool(b)
        case dt: DateTime => VDate(dt)
        case e: Enum[_] => VStr(e.companion.stringConverter.asInstanceOf[Converter[Any]].toString(e))
        case _: Any => {
          val converter = stringConverters.find(TypeInfo(a.getClass)).asInstanceOf[StringConverters.Converter[Any]]
          VStr(converter.toString(a))
        }
      }
      wrap(m.invoke(a))
    }.getOrError(s"cannot find property ${propertyName} in ${a.getClass} -- ${a}")
  }
}
