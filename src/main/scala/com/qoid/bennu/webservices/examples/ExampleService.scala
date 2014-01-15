package com.qoid.bennu.webservices.examples

import net.model3.guice.bootstrap.ApplicationName
import com.google.inject.Inject
import m3.servlet.beans.Parm

case class ExampleService @Inject() (
  appName: ApplicationName, // an example of something injected,  if it doesn't have @Parm it is injected from Guice
  @Parm p1: String,
  @Parm p2: List[String],
  @Parm p3: Option[String],              // an example of an optional parm
  @Parm p4: String = "a default value"  // an alternate way to do an optional parm
) {

  def service = {
    s"""<html><body><pre>
    An example service
    
    The application name = ${appName}
    
    p1 = ${p1}
    p2 = ${p2.mkString(" -- ")}
    p3 = ${p3}
    p4 = ${p4}
    
    The response handler handles common return types so it will just push this string into the response
    
    </pre></body></html>"""
  }
  
  
}