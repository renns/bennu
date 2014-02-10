package com.qoid.bennu.testclient


import m3.predef._
import m3.servlet.beans.Mappings
import m3.CaseClassReflector
import net.model3.lang.ClassX
import m3.servlet.beans.CaseClassBeanFactory
import m3.servlet.ParmConverters
import m3.json.Serialization
import com.google.inject.Injector
import m3.TypeInfo

object ClientCodeGen extends App {

  val mappings = inject[Mappings]
  
  mappings.pathMappings.foreach { pm =>
    val m = caseClass(pm)
    println(m)
  }
  
  /**
   * Limited implementation only covers the types seen so far
   */
  def resolveTypeName(ti: TypeInfo) = {
    ti match {
      case TypeInfo(pc, Nil) if pc.isPrimitive => {
        pc.getName.toLowerCase match {
          case "int" => "Int"
          case "boolean" => "Boolean" 
        }
      }
      case _ => ti.typeName
    }
  }

  /**
   * Limited implementation only covers the types seen so far
   */
  def resolveDefaultValue(defaultValue: Option[AnyRef]) = {
    defaultValue match {
      case None => ""
      case Some(v) => " = " + (v match {
        case s: String => '"' + s + '"'
        case _ => v.toString()
      })
    }
  }
  
  def caseClass(mapping: Mappings.PathMapping): String = {
    
    val ccbf = CaseClassBeanFactory(
      beanClass = mapping.clazz, 
      parmConverters = inject[ParmConverters],
      jsonSerializer = inject[Serialization.Serializer],
      injector = inject[Injector]
    )
    
    val parms = ccbf.parms.sortBy(_.parmIndex).map { p =>
      s"${p.name}: ${resolveTypeName(p.property.typeInfo)}${resolveDefaultValue(p.defaultValue)}"
    }.mkString(", ")
    
    val cname = ClassX.getShortName(mapping.clazz)
    val methodName = cname.take(1).toLowerCase + cname.drop(1)
    
    s"""
  case class ${cname}(${parms})
    """
    
  }
  
}