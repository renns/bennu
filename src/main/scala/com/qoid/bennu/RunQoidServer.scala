package com.qoid.bennu

import net.model3.servlet.runner.JettyRunner
import com.google.inject.spi.Dependency
import net.model3.guice.DependencyInjector
import m3.predef._
import net.model3.guice.LifeCycleManager
import net.model3.guice.LifeCycleListeners
import com.qoid.bennu.util.HsqldbServerStarterUpper

object RunQoidServer extends App {

  System.setProperty("longLivedApp", "true")
  
  JettyRunner.main(args)

}
