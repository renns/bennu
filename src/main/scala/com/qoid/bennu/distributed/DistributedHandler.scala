package com.qoid.bennu.distributed

import m3.predef.ScalaInjector

trait DistributedHandler {
  def handle(message: DistributedMessage, injector: ScalaInjector): Unit
}
