package com.qoid.bennu.squery.ast

import com.qoid.bennu.model.id.InternalId
import com.qoid.bennu.security.AgentView
import com.qoid.bennu.security.SecurityContext
import java.sql.{ Connection => JdbcConn }
import m3.Chord
import m3.Chord._
import m3.predef._
import m3.predef.box._

object ContentQuery {
  
  val transformer: PartialFunction[Node,Chord] = {
    case fc: FunctionCall if functions.contains(fc.name) => functions(fc.name)(fc)
  }

  private val functions = Map[String,FunctionCall => Chord](
    "hasLabelPath" -> fn_hasLabelPath,
    "hasLabel" -> fn_hasLabel,
    "hasConnectionMetaLabel" -> fn_hasConnectionMetaLabel
  )
  
  private def fn_hasLabelPath(fc: FunctionCall): Chord = {
    implicit val jdbcConn = inject[JdbcConn]
    val path = stringLiterals(fc.parms)
    val av = inject[AgentView]
    val label = av.resolveLabel(path)
    fn_contentHasLabelOrDescendant(label.map(_.iid))
  }

  private def fn_hasLabel(fc: FunctionCall): Chord = {
    fn_contentHasLabelOrDescendant(Full(InternalId(stringLiteral(fc.parms))))
  }

  private def fn_hasConnectionMetaLabel(fc: FunctionCall): Chord = {
    val av = inject[AgentView]
    val labelIid = av.resolveConnectionMetaLabel()

    val whereClause = labelIid match {
      case Full(iid) => Chord(s"labelIid = '${iid.value}'")
      case _ => Chord("1 <> 1")
    }

    "iid in (select contentIid from labeledcontent where " ~ whereClause ~ ")"
  }

  private def fn_contentHasLabelOrDescendant(parentLabelIid: Box[InternalId]): Chord = {
    implicit val av = inject[AgentView]
    implicit val jdbcConn = inject[JdbcConn]
    
    val ancestry = parentLabelIid.map(l=>SecurityContext.resolveLabelAncestry(l).toList).getOrElse(Nil)
    
    val whereClause = ancestry match {
      case Nil => Chord("1 <> 1")
      case l => "labelIid in (" ~ l.map("'" ~ _.value ~ "'").mkChord(",") ~ ")" 
    }

    "iid in (select contentIid from labeledcontent where " ~ whereClause ~ ")"
  }

  def stringLiterals(parms: List[Node]): List[String] = parms match {
    case Nil => Nil
    case StringLit(s) :: tl => s :: stringLiterals(tl)
    case _ => m3x.error(s"don't know how to handle -- ${parms}")
  }
  
  def stringLiteral(parms: List[Node]): String = parms match {
    case List(StringLit(s)) => s
    case _ => m3x.error(s"don't know how to handle -- ${parms}")
  } 
}
