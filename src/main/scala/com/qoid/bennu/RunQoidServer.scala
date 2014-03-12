package com.qoid.bennu

import com.qoid.bennu.distributed.DistributedManager
import java.sql.Connection
import m3.Txn
import m3.predef._
import net.model3.servlet.runner.JettyRunner

object RunQoidServer extends App {

  System.setProperty("longLivedApp", "true")

  Txn(inject[DistributedManager].initialize(inject[Connection]))

  JettyRunner.main(args)

}
