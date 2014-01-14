package com.qoid.bennu.webservices

import m3.servlet.M3ServletModule
import m3.servlet.CurlFilter
import m3.servlet.RequestDumpFilter
import m3.servlet.compression.CompressionFilter
import m3.servlet.scalate.ScalateFilter
import m3.servlet.RootServletFilter
import m3.servlet.upload.FileUploadFilter
import m3.servlet.TransactionFilter



class WebServicesModule extends M3ServletModule {

  
  override def configureServlets = {
    
    // log a curl command for each request to the api (good for dev, BAD for production)
    filter("/api/*").through(classOf[CurlFilter])
    
    filter("/*").through(classOf[CompressionFilter])
    
    // support multipart content types
    filter("/*").through(classOf[FileUploadFilter]) 
    
    // log uncaught exceptions with some intelligence about how jetty works
    filter("/*").through(classOf[RootServletFilter])
    
    // uncomment if you want lots of logging for each request (useful for dev/debugging, BAD for production)
    //filter("/*").through(classOf[RequestDumpFilter])
    
    // wrap every request in a transaction, there are several things that hang their hats on transactions
    filter("/*").through(classOf[TransactionFilter])
    
    // we use scalate for our templating needs think (jsp - java) + scala = scalate -- http://scalate.fusesource.org/
    filter("*.ssp", "*.html").through(classOf[ScalateFilter])
    
    
    serveBean[DoSomethingWithDatabase]("/api/doSomethingWithDatabase")
    serveBean[ExampleService]("/api/exampleService")
    
    addServletBeanFilter
    
    
  }
  
  
  
}