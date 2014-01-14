

To run the server run com.ahsrcm.entdb.RunEntdbServer.  That runs an embedded jetty server.

The bootstrapping is provided by guice and proceeds as follows

find any guice-modules.list files in the classpath.  Find the classes listed in there with the highest rank.  So one file with

100 xyz.pdq.MyGuiceModule

and in another you had

999 bbb.ccc.AnotherGuiceModule


bbb.ccc.AnotherGuiceModule would get loaded because it is ranked higher.  The module resolution will be logged so you can properly debug.

The first thing the bootstrapping does is configure a temporary logger and logging is done into memory.  It uses this so the bootstrapping code can "just log".  The bootstrapping configures logging and runs the various lifecycle events (bootstrap and config) and then eventually starts up the jetty server.  The jetty server starts up sees the lone GuiceFilter in web.xml and that then kicks off the guice based servlet config.  

The two guice modules.  The main bootstrap module com.ahsrcm.entdb.GuiceModule and the servlet config module com.ahsrcm.entdb.webservices.WebServicesModule.  WebServicesModule is effectively the web.xml of a guice servlet app.

Because this is built on top of the model3 project(s) there is quite a long list of things sort of confgiured through convention via guice. 

 * logging
 * transaction manager
 * database pooling
 * servlet parameter mapping
 * json serialization
 * mapping types to/from database fields
 * scala specific stuff
  * transaction enhancements Txn.scala
  * m3.jdbc jdbc enhancements


