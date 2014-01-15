package com.qoid.bennu.webservices

import m3.servlet.M3ServletModule
import m3.servlet.CurlFilter
import m3.servlet.RequestDumpFilter
import m3.servlet.compression.CompressionFilter
import m3.servlet.scalate.ScalateFilter
import m3.servlet.RootServletFilter
import m3.servlet.upload.FileUploadFilter
import m3.servlet.TransactionFilter
import com.qoid.bennu.webservices.examples.ExampleService
import com.qoid.bennu.webservices.examples.DoSomethingWithDatabase
import com.qoid.bennu.webservices.examples.AdditionService
import com.qoid.bennu.webservices.examples.MultiplicationService
import m3.servlet.longpoll.webservice.ChannelCreate
import m3.servlet.longpoll.webservice.ChannelPoll
import m3.servlet.longpoll.webservice.SubmitChannelRequests



class WebServicesModule extends M3ServletModule {

  
  override def configureServlets = {
    
    // log a curl command for each request to the api (good for dev, BAD for production)
    filter("/api/*").through(classOf[CurlFilter])
    
    // currently this breaks long polling so we have it commented out
//    filter("/*").through(classOf[CompressionFilter])
    
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
    
    
    serveBean[ChannelCreate]("/api/channel/create")
    serveBean[ChannelPoll]("/api/channel/poll")
    serveBean[SubmitChannelRequests]("/api/channel/submit")
    
    serveBean[DoSomethingWithDatabase]("/api/example/doSomethingWithDatabase")
    serveBean[ExampleService]("/api/example/exampleService")
    
    serveBean[AdditionService]("/api/example/add")
    serveBean[MultiplicationService]("/api/example/multiply")
    
    addServletBeanFilter
    
    
  }
  
  
  
}