package com.qoid.bennu.util

import net.model3.lang.TimeDuration
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.Promise

class FutureExtensions[T](f: Future[T]) {
  def withTimeout(timeout: TimeDuration)(implicit ec: ExecutionContext): Future[T] = {
    if (timeout.inMilliseconds() > 0) {
      val p = Promise[T]()
      val t = TimeoutScheduler.scheduleTimeout(p, timeout)
      f.onComplete { case _ => t.cancel() }
      Future.firstCompletedOf(List(f, p.future))
    } else {
      f
    }
  }
}
