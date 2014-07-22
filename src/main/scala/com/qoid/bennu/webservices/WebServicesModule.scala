package com.qoid.bennu.webservices

import com.qoid.bennu.ServicePath
import com.qoid.bennu.webservices.v1.CreateAgent
import com.qoid.bennu.webservices.v1.Query
import m3.servlet.CorsFilter
import m3.servlet.CurlFilter
import m3.servlet.M3ServletModule
import m3.servlet.RootServletFilter
import m3.servlet.TransactionFilter
import m3.servlet.compression.CompressionFilter
import m3.servlet.upload.FileUploadFilter

class WebServicesModule extends M3ServletModule {

  override def configureServlets = {

    filter("/api/*").through(classOf[CorsFilter])

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
//    filter("*.ssp", "*.html").through(classOf[ScalateFilter])

    serveBean[CreateAgent](ServicePath.createAgent)
//    serveBean[DeleteAgent](ServicePath.deleteAgent)
//    serveBean[ImportAgent](ServicePath.importAgent)
//
//    serveBean[Login](ServicePath.login)
//    serveBean[Logout](ServicePath.logout)
//
//    serveBean[PollChannel](ServicePath.pollChannel)
//    serveBean[SubmitChannelRequests](ServicePath.submitChannel)
//
//    serveBean[UpsertService](ServicePath.upsert)
//    serveBean[DeleteService](ServicePath.delete)

    serveBean[Query](ServicePath.query)

//    serveBean[InitiateIntroductionService](ServicePath.initiateIntroduction)
//    serveBean[RespondToIntroductionService](ServicePath.respondToIntroduction)
//
//    serveBean[RequestVerificationService](ServicePath.requestVerification)
//    serveBean[RespondToVerificationService](ServicePath.respondToVerification)
//    serveBean[VerifyService](ServicePath.verify)
//    serveBean[AcceptVerificationService](ServicePath.acceptVerification)

    addServletBeanFilter
  }
}
