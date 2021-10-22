package com.utils

import java.util.regex.Pattern
import javax.inject.{Inject, Singleton}
import com.intellivision.auth._
import org.apache.commons.lang3.StringUtils
import play.api.{Configuration, Logger}
import play.api.mvc.{Headers, RequestHeader}

import scala.concurrent.Future
import scala.collection.JavaConversions._
import scala.util.matching.Regex

@Singleton
class RequestAuthenticator @Inject()(regexUtils : RegexUtils,
                                     concurrencyUtils: ConcurrencyUtils,
                                     configuration: Configuration){

  import concurrencyUtils.ec

  private val authToken = configuration.getString("AUTH_TOKEN").getOrElse("$;yhD&88i]fG657hy8#!ji978}aLR0|")

  def validate(requestHeader: RequestHeader): Future[Either[AuthResult, RequestHeader]] = {
    if (canSkipAuthentication(requestHeader)) {
      Future.successful(Right(requestHeader))
    }
    else {
      val headers = requestHeader.headers.toSimpleMap
      val isValid = headers.get("date").exists { date =>
        headers.get("token").exists { tokenHash =>
          SHA.matches(authToken, date, tokenHash)
        }
      }
      // This is for time being otherwise above approach will be followed
      //    val isValid = headers.get("token").exists(_ == authToken)

      val response = if (isValid) Right(requestHeader)
      else {
        Logger.warn(s"Token: [${headers.get("token")}] is invalid")
        Left(Forbidden)
      }
      Future.successful(response)
    }
    Future.successful(Right(requestHeader))
  }

  private def canSkipAuthentication(request: RequestHeader): Boolean = {
    isPingRequest(request) || isNotifyRequest(request)
    true
  }

  private def isPingRequest(request: RequestHeader): Boolean =
    request.path.endsWith("loscars/ping") && request.method.equalsIgnoreCase("GET")

  private def isNotifyRequest(request: RequestHeader): Boolean =
    request.path.endsWith("/loscars/notify") && request.method.equalsIgnoreCase("POST")

}
