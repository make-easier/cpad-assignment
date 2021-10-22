package com.utils

import javax.inject.{Inject, Singleton}

import play.api.Logger
import play.api.http.HttpErrorHandler
import play.api.http.MimeTypes.XML
import play.api.mvc.Results.Status
import play.api.mvc.{RequestHeader, Result}

import scala.concurrent.Future

@Singleton
class ErrorHandler @Inject() extends HttpErrorHandler {

  private val errorXml = """<result><status>failure</status><message>error occurred while processing request</message></result>"""
  private val errorJson = """{"status": "failure", "message": "error occurred while processing request"}"""

  override def onClientError(request: RequestHeader, statusCode: Int, message: String): Future[Result] = {
    Logger.error(s"client-error: [$statusCode, $message] occurred in request: [${request.uri}, ${request.method}]")
    val result = Status(statusCode)(if (request.accepts(XML)) errorXml else errorJson)
    Future.successful(result)
  }

  override def onServerError(request: RequestHeader, exception: Throwable): Future[Result] = {
    Logger.error(s"server-error occurred in request: [${request.uri}, ${request.method}]", exception)
    val result = if (request.accepts(XML)) Status(500)(errorXml) else Status(500)(errorJson)
    Future.successful(result)
  }
}
