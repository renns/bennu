package com.qoid.bennu.query.ast

import com.qoid.bennu.model.assist.LabelAssist
import com.qoid.bennu.model.id.InternalId
import m3.Chord
import m3.Chord._
import m3.predef._
import m3.predef.box._

object ContentQuery {
  private val labelAssist = inject[LabelAssist]

  private val functions = Map[String,FunctionCall => Chord](
    "hasLabelPath" -> fn_hasLabelPath,
    "hasLabel" -> fn_hasLabel,
    "hasConnectionMetaLabel" -> fn_hasConnectionMetaLabel
  )
  
  val transformer: PartialFunction[Node,Chord] = {
    case fc: FunctionCall if functions.contains(fc.name) => functions(fc.name)(fc)
  }

  private def fn_hasLabelPath(fc: FunctionCall): Chord = {
    val path = fc.parms.map(Node.nodeToString)
    val labelIid = labelAssist.resolveLabel(path)

    fn_contentHasLabelOrDescendant(labelIid)
  }

  private def fn_hasLabel(fc: FunctionCall): Chord = {
    val parm = fc.parms match {
      case node :: Nil => Node.nodeToString(node)
      case _ => m3x.error(s"invalid parameters -- ${fc.parms}")
    }

    fn_contentHasLabelOrDescendant(Full(InternalId(parm)))
  }

  private def fn_hasConnectionMetaLabel(fc: FunctionCall): Chord = {
    val labelIid = labelAssist.resolveConnectionMetaLabel()

    val whereClause = labelIid match {
      case Full(iid) => Chord(s"labelIid = '${iid.value}'")
      case _ => Chord("1 <> 1")
    }

    "iid in (select contentIid from labeledcontent where " ~ whereClause ~ ")"
  }

  private def fn_contentHasLabelOrDescendant(parentLabelIid: Box[InternalId]): Chord = {
    val ancestry = parentLabelIid.map(l => labelAssist.resolveLabelAncestry(l).toList).getOrElse(Nil)
    
    val whereClause = ancestry match {
      case Nil => Chord("1 <> 1")
      case l => "labelIid in (" ~ l.map("'" ~ _.value ~ "'").mkChord(",") ~ ")"
    }

    "iid in (select contentIid from labeledcontent where " ~ whereClause ~ ")"
  }
}
