package com.qoid.bennu.webservices.examples

import net.model3.guice.bootstrap.ApplicationName
import com.google.inject.Inject
import m3.servlet.beans.Parm
import net.liftweb.json.JInt

case class MultiplicationService @Inject() (
  @Parm left: Int,
  @Parm right: Int
) {

  def service = JInt(left*right)
  
}