package com.qoid.bennu.query.ast

import com.qoid.bennu.model.assist.LabelAssist
import com.qoid.bennu.model.id.InternalId
import m3.Chord
import m3.Chord._
import m3.jdbc._
import m3.predef._
import m3.predef.box._

object LabelQuery {
  private val labelAssist = inject[LabelAssist]

  private val functions = Map[String,FunctionCall => Chord](
    "hasParentLabelPath" -> fn_hasParentLabelPath,
    "hasParentLabel" -> fn_hasParentLabel
  )
  
  val transformer: PartialFunction[Node,Chord] = {
    case fc: FunctionCall if functions.contains(fc.name) => functions(fc.name)(fc)
  }

  private def fn_hasParentLabelPath(fc: FunctionCall): Chord = {
    val path = fc.parms.map(Node.nodeToString)
    val labelIid = labelAssist.resolveLabel(path)
    hasParentLabel(labelIid)
  }

  private def fn_hasParentLabel(fc: FunctionCall): Chord = {
    val parm = fc.parms match {
      case node :: Nil => Node.nodeToString(node)
      case _ => m3x.error(s"invalid parameters -- ${fc.parms}")
    }

    hasParentLabel(Full(InternalId(parm)))
  }

  private def hasParentLabel(parentLabelIid: Box[InternalId]): Chord = {
    parentLabelIid match {
      case Full(iid) => sql"iid in (select childIid from labelchild where parentIid = ${iid})"
      case _ => Chord("1 <> 1")
    }
  }
}
