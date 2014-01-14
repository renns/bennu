package com.ahsrcm.entdb

import scala.beans.BeanProperty
import com.google.inject.ProvidedBy
import com.google.inject.Provider
import javax.inject.Inject
import net.model3.xstream.XmlSerializableConfig
import m3.jdbc.Database

object Config {

}

case class Config(
    database: Database
)
