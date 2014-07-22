package com.qoid.bennu

class BennuException(
  errorCode: String,
  message: String,
  cause: Throwable
) extends RuntimeException(message, cause) {

  def this(errorCode: String) = this(errorCode, "", null)
  def this(errorCode: String, message: String) = this(errorCode, message, null)
  def this(errorCode: String, cause: Throwable) = this(errorCode, "", cause)

  def getErrorCode(): String = errorCode

  override def getMessage: String = {
    var list = List.empty[String]

    if (super.getMessage != null && super.getMessage.nonEmpty) list = super.getMessage :: list
    if (errorCode != null && errorCode.nonEmpty) list = errorCode :: list

    list.mkString(" -- ")
  }
}
