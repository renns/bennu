package com.qoid.bennu.util

import io.netty.util.HashedWheelTimer
import io.netty.util.Timeout
import io.netty.util.TimerTask
import java.util.concurrent.TimeUnit
import net.model3.lang.TimeDuration
import scala.concurrent.Promise
import scala.concurrent.TimeoutException

object TimeoutScheduler {
  val timer = new HashedWheelTimer(100, TimeUnit.MILLISECONDS)

  def scheduleTimeout(p: Promise[_], timeout: TimeDuration): Timeout = {
    val timerTask = new TimerTask {
      override def run(t: Timeout): Unit = {
        p.failure(new TimeoutException(s"Operation timed out after ${timeout.inMilliseconds()} milliseconds."))
      }
    }

    timer.newTimeout(timerTask, timeout.inMilliseconds(), TimeUnit.MILLISECONDS)
  }
}
