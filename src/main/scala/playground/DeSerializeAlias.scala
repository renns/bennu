package playground

import com.qoid.bennu.JsonAssist._
import com.qoid.bennu.model.Alias
import m3.guice.GuiceApp

object DeSerializeAlias extends GuiceApp {

  val json = """{"iid":"LYvNJqUxqFnqmiWWiz2SwrUfJhVIDnIn","rootLabelIid":"qoid","name":"ssss","data":{"name":"","imgSrc":""}}"""

  val jv = parseJson(json)
  
  val alias = jv.deserialize[Alias]
  
  println(alias.toString)
  
}