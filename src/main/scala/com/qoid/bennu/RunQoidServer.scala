package com.qoid.bennu

import com.qoid.bennu.distributed.DistributedManager
import m3.Txn
import m3.predef._
import net.model3.servlet.runner.JettyRunner

object RunQoidServer extends App {

  System.setProperty("longLivedApp", "true")

  Txn(inject[DistributedManager].initialize())

  JettyRunner.main(args)

}
