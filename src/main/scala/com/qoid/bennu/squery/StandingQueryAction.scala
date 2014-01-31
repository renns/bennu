package com.qoid.bennu.squery

import m3.StringConverters._

sealed trait StandingQueryAction

object StandingQueryAction extends HasStringConverter {
  case object Insert extends StandingQueryAction
  case object Update extends StandingQueryAction
  case object Delete extends StandingQueryAction
  case class Unknown(value: String) extends StandingQueryAction

  val stringConverter = new Converter[StandingQueryAction] {
    override def toString(value: StandingQueryAction): String = value.toString.toLowerCase

    override def fromString(value: String): StandingQueryAction = {
      value.toLowerCase match {
        case "insert" => StandingQueryAction.Insert
        case "update" => StandingQueryAction.Update
        case "delete" => StandingQueryAction.Delete
        case _ => StandingQueryAction.Unknown(value)
      }
    }
  }
}
