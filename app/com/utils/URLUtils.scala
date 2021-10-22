//package com.utils
//
//import java.net.{URI, URL}
//
//import org.apache.http.client.utils.URIBuilder
//import play.api.Logger
//
//import scala.util.{Failure, Success, Try}
//import scala.util.matching.Regex
//
//object URLUtils {
//  def appendQueryParams(strUrl: String, queryParams: Map[String, String]): String = {
//    val uriBuilder = new URIBuilder(strUrl)
//    queryParams.foreach { case (key, value) =>
//      uriBuilder.addParameter(key, value)
//    }
//    uriBuilder.build().toString
//  }
//
////  val urlPattern: Regex = "((?:http|https)+:/{2}?(www[.{1}])?([A-Za-z0-9._%+-]+)/?.*)".r
//
//  def isUrlValid(url: String): Boolean = {
////    urlPattern.findFirstMatchIn(url) match {
////      case Some(_) =>
////        additional check is required
////      case None =>
////        Logger.warn(url + " is invalid url.")
////        false
////    }
//    val uri = new URL(url)
//    Try(uri.toURI) match {
//      case Success(value) => true
//      case Failure(ex) => Logger.warn(s"$url is invalid URL", ex)
//        false
//    }
//  }
//
//  def isUrlsValid(urls: String*): Boolean = {
//    !urls.map(url => URLUtils.isUrlValid(url)).contains(false)
//  }
//
//}
