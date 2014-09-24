package com.qoid.bennu

import com.qoid.bennu.distributed.DistributedManager
import m3.Txn
import m3.predef._
import net.model3.servlet.runner.JettyRunner

object RunQoidServer extends App {

  // Uncomment line below to disable logging DEBUG and INFO messages
  //net.model3.logging.LoggerHelper.getRootLogger.setLevel(net.model3.logging.Level.WARN)

  System.setProperty("longLivedApp", "true")

  Txn(inject[DistributedManager].initialize())

  JettyRunner.main(args)

}
