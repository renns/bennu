package com.qoid.bennu.testclient

import m3.guice.GuiceApp

object IntegratorRunner extends GuiceApp {
  val results = run()

  println("\nResults:")

  results.foreach {
    case (name, None) => println(s"  $name -- PASS")
    case (name, Some(e)) => println(s"  $name -- FAIL\n${e.getMessage}\n${e.getStackTraceString}")
  }

  System.exit(0)

  def run(): List[(String, Option[Exception])] = {
    AliasLoginIntegrator.run() ++
    DeleteIntegrator.run() ++
    DeRegisterIntegrator.run() ++
    IntroductionIntegrator.run() ++
    ProfilesIntegrator.run() ++
    QueryIntegrator.run() ++
    UpsertIntegrator.run()
  }
}
