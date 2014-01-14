package com.qoid.bennu.schema

object SchemaCompare extends App {

  import Settings._

  printDdl(schemaManager.upradeDdl.map(_.toString))

}

