package com.qoid.bennu.mapper

import com.qoid.bennu.model._
import m3.predef._

object MapperAssist {
  lazy val allMappers: List[BennuMapperCompanion[_]] = List(
    Agent,
    Alias,
    Connection,
    Content,
    Introduction,
    Label,
    LabelAcl,
    LabelChild,
    LabeledContent,
    Login,
    Notification,
    Profile
  )

  def findMapperByTypeName(typeName: String): BennuMapperCompanion[_] = {
    allMappers.find(_.typeName =:= typeName).getOrError(s"don't know how to handle type ${typeName}")
  }

  def findMapperByType[T : Manifest]: BennuMapperCompanion[_] = {
    findMapperByTypeName(manifest[T].runtimeClass.getSimpleName)
  }
}
