package com.qoid.bennu

case class ServiceException(
  message: String,
  errorCode: ErrorCode,
  parms: Option[String] = None
) extends Exception(message)
