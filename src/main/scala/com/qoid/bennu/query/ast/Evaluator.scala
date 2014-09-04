package com.qoid.bennu.query.ast

import com.qoid.bennu.Enum
import com.qoid.bennu.model.Content
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.LabelChild
import com.qoid.bennu.model.LabeledContent
import com.qoid.bennu.model.assist.LabelAssist
import com.qoid.bennu.model.id.InternalId
import m3.StringConverters
import m3.StringConverters.Converter
import m3.TypeInfo
import m3.predef._
import m3.predef.box._
import net.model3.chrono.DateTime

object Evaluator extends Logging {
  private val stringConverters = inject[StringConverters]
  private val labelAssist = inject[LabelAssist]

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
    fc.name match {
      case "hasLabelPath" =>
        val path = fc.parms.map(Node.nodeToString)
        labelAssist.resolveLabel(path) match {
          case Full(iid) => impl.hasLabel(row, iid, true)
          case _ => VFalse
        }

      case "hasLabel" =>
        val parm = fc.parms match {
          case node :: Nil => Node.nodeToString(node)
          case _ => m3x.error(s"invalid parameters -- ${fc.parms}")
        }

        impl.hasLabel(row, InternalId(parm), true)

      case "hasConnectionMetaLabel" =>
        labelAssist.resolveConnectionMetaLabel() match {
          case Full(iid) => impl.hasLabel(row, iid, false)
          case _ => VFalse
        }

      case "hasParentLabelPath" =>
        val path = fc.parms.map(Node.nodeToString)
        labelAssist.resolveLabel(path) match {
          case Full(iid) => impl.hasParentLabel(row, iid)
          case _ => VFalse
        }

      case "hasParentLabel" =>
        val parm = fc.parms match {
          case node :: Nil => Node.nodeToString(node)
          case _ => m3x.error(s"invalid parameters -- ${fc.parms}")
        }

        impl.hasParentLabel(row, InternalId(parm))

      case _ => m3x.error("function call not supported -- " + reify(fc))
    }
  }
  
  object impl {
    def hasLabel(row: Any, labelIid: InternalId, recursive: Boolean): VBool = {
      row match {
        case c: Content =>
          val labelIids = if (recursive) {
            labelAssist.resolveLabelAncestry(labelIid).toList
          } else {
            List(labelIid)
          }

          val labeledContents = LabeledContent.select("labelIid in (" + labelIids.map("'" + _.value + "'").mkString(",") + ")")

          if (labeledContents.exists(_.contentIid == c.iid)) VTrue else VFalse

        case _ => VFalse
      }
    }

    def hasParentLabel(row: Any, labelIid: InternalId): VBool = {
      row match {
        case l: Label =>
          val labelChilds = LabelChild.select(s"parentIid = '${labelIid}'")
          if (labelChilds.exists(_.childIid == l.iid)) VTrue else VFalse

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
