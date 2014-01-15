package com.qoid.bennu.webservices

import com.google.inject.Inject
import java.sql.Connection
import m3.jdbc._
import m3.json.Streamer._
import m3.predef._
import m3.servlet.beans.Parm
import com.qoid.bennu.model.Label
import com.qoid.bennu.model.Alias
import com.qoid.bennu.model.Content
import m3.json.JsonSerializer
import net.liftweb.json.JValue
import com.qoid.bennu.model.InternalId
import com.qoid.bennu.model.HasInternalId

case class UpsertService @Inject() (
  conn: Connection,
  serializer: JsonSerializer,
  @Parm("type") _type: String,
  @Parm instance: JValue
) extends Logging {
 
  implicit def _conn = conn
  
/*
  Need to deserialize json into _type
  What are the available actions?
  Create
    Who generates the InternalId?
    If client does, do we need to check to make sure it isn't already in use?
  Read
    What would the json be?
    Is there a case where we only want to get one item (instead of a list)?
  Update
    Do we check to make sure the data exists?
  Delete
    What would the json be?
    What happens when something is deleted that others have a relationship to?
*/
  
  def service = {
    
    val (mapper0, i0) = _type.toLowerCase() match {
      case "alias" => Alias -> serializer.fromJson[Alias](instance)
      case "content" => Content -> serializer.fromJson[Content](instance)
      case _ => m3x.error(s"don't know how to handle type ${_type}")
    }
    
    val mapper = mapper0.asInstanceOf[Mapper[HasInternalId,InternalId]]
    val i = i0.asInstanceOf[HasInternalId]
    
    mapper.fetchOpt(i.iid) match {
      case None => mapper.insert(i)
      case Some(v) => mapper.update(v)
    }
    
  }
}
