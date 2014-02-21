package com.qoid.bennu.util

import scala.concurrent.Future
import scala.language.implicitConversions

object Implicits {
  implicit def futureExtensions[T](f: Future[T]): FutureExtensions[T] = new FutureExtensions(f)
}
