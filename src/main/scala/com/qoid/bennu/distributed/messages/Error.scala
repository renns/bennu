package com.qoid.bennu.distributed.messages

import com.qoid.bennu.ToJsonCapable

case class Error(
  errorCode: String,
  message: String
) extends ToJsonCapable
