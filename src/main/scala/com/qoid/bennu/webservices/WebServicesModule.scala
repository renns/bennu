package com.qoid.bennu.webservices

import com.qoid.bennu.ServicePath
import m3.servlet.CorsFilter
import m3.servlet.CurlFilter
import m3.servlet.M3ServletModule
import m3.servlet.RootServletFilter
import m3.servlet.TransactionFilter
import m3.servlet.compression.CompressionFilter
import m3.servlet.upload.FileUploadFilter

class WebServicesModule extends M3ServletModule {

  override def configureServlets() = {

    filter("/api/*").through(classOf[CorsFilter])

    // log a curl command for each request to the api (good for dev, BAD for production)
    filter("/api/*").through(classOf[CurlFilter])

    filter("/*").through(classOf[CompressionFilter])

    // support multi-part content types
    filter("/*").through(classOf[FileUploadFilter])

    // log uncaught exceptions with some intelligence about how jetty works
    filter("/*").through(classOf[RootServletFilter])

    // uncomment if you want lots of logging for each request (useful for dev/debugging, BAD for production)
    //filter("/*").through(classOf[RequestDumpFilter])

    // wrap every request in a transaction, there are several things that hang their hats on transactions
    filter("/*").through(classOf[TransactionFilter])

    // we use scalate for our templating needs think (jsp - java) + scala = scalate -- http://scalate.fusesource.org/
    //filter("*.ssp", "*.html").through(classOf[ScalateFilter])

    // Agent
    serveBean[v1.CreateAgent](ServicePath.createAgent)
//    serveBean[DeleteAgent](ServicePath.deleteAgent)
//    serveBean[ImportAgent](ServicePath.importAgent)

    // Session
    serveBean[v1.Login](ServicePath.login)
    serveBean[v1.Logout](ServicePath.logout)

    // Channel
    serveBean[v1.PollChannel](ServicePath.pollChannel)
    serveBean[v1.SubmitChannelRequests](ServicePath.submitChannelRequests)

    // Query
    serveBean[v1.Query](ServicePath.query)
//    serveBean[CancelQuery](ServicePath.cancelQuery)

    // Alias
    serveBean[v1.CreateAlias](ServicePath.createAlias)
    serveBean[v1.UpdateAlias](ServicePath.updateAlias)
    serveBean[v1.DeleteAlias](ServicePath.deleteAlias)
    serveBean[v1.CreateAliasLogin](ServicePath.createAliasLogin)
    serveBean[v1.UpdateAliasLogin](ServicePath.updateAliasLogin)
    serveBean[v1.DeleteAliasLogin](ServicePath.deleteAliasLogin)
    serveBean[v1.UpdateAliasProfile](ServicePath.updateAliasProfile)

    // Connection

    // Content
    serveBean[v1.CreateContent](ServicePath.createContent)

    // Label
    serveBean[v1.CreateLabel](ServicePath.createLabel)

    // Notification

    // Introduction
//    serveBean[InitiateIntroductionService](ServicePath.initiateIntroduction)
//    serveBean[RespondToIntroductionService](ServicePath.respondToIntroduction)

    // Verification
//    serveBean[RequestVerificationService](ServicePath.requestVerification)
//    serveBean[RespondToVerificationService](ServicePath.respondToVerification)
//    serveBean[VerifyService](ServicePath.verify)
//    serveBean[AcceptVerificationService](ServicePath.acceptVerification)

    addServletBeanFilter
  }
}
