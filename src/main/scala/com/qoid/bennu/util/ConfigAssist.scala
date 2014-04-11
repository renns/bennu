package com.qoid.bennu.util

import com.typesafe.config._
import com.qoid.bennu.JsonAssist._

object ConfigAssist {
  def parseHocon(hocon: String): Config = {

    val parseOptions =
      ConfigParseOptions.
        defaults.
        setAllowMissing(false).
        setSyntax(ConfigSyntax.CONF)

    val defaultOverrides = if (ConfigFactory.defaultOverrides().hasPath("bennu")) {
      val c = ConfigFactory.defaultOverrides().getConfig("bennu")
      logger.debug(s"system properties:\n${m3.json.ConfigAssist.toJValue(c).toJsonStr}")
      c
    } else {
      ConfigFactory.empty()
    }

    val config = ConfigFactory.parseString(hocon, parseOptions)

    logger.debug(s"config file:\n${m3.json.ConfigAssist.toJValue(config).toJsonStr}")

    val mergedConfig = defaultOverrides.withFallback(config)

    logger.debug(s"using config:\n${m3.json.ConfigAssist.toJValue(mergedConfig).toJsonStr}")

    mergedConfig
  }

  def parseHoconToJson(hocon: String): JValue = m3.json.ConfigAssist.toJValue(parseHocon(hocon))
}
