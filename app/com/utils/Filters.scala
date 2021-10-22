package com.utils

import javax.inject.{Inject, Singleton}
import akka.stream.Materializer
import akka.util.ByteString
import com.intellivision.auth.{Auth, Forbidden, Ok, Unauthorized}
import play.api.libs.iteratee.{Enumeratee, Iteratee}
//import org.json4s.Xml.toJson
//import org.json4s.native.JsonMethods.{compact, render}
import play.api.Logger
import play.api.http.{DefaultHttpFilters, MimeTypes}
import play.api.http.MimeTypes.{JSON, XML}
import play.api.libs.iteratee.{Enumeratee, Iteratee}
import play.api.libs.json._
import play.api.libs.streams.Accumulator
import play.api.libs.streams.Accumulator
import play.api.mvc.{RequestHeader, Result, _}
import play.http.HttpEntity
import play.mvc.Http.HeaderNames.CONTENT_TYPE

import scala.concurrent.Future
import scala.xml.{XML => XMLUtil, _}

@Singleton
class Filters @Inject()(authFilter: AuthFilter)extends DefaultHttpFilters(authFilter){
}

//@Singleton
//class JsonFilter @Inject()(implicit mat: Materializer, concurrencyUtils: ConcurrencyUtils) extends EssentialFilter {
//
//  import concurrencyUtils.ec
//
//  val xmlHeader: Tuple2[String, String] = CONTENT_TYPE -> XML
//
//  implicit def accumulatorToIteratee(accumulator: Accumulator[ByteString, Result]): Iteratee[ByteString, Result] =
//    Streams.accumulatorToIteratee(accumulator)
//
//  implicit def iterateeToAccumulator(iteratee: Iteratee[ByteString, Result]): Accumulator[ByteString, Result] =
//    Streams.iterateeToAccumulator(iteratee)
//
//  override def apply(nextFilter: EssentialAction): EssentialAction = new EssentialAction {
//
//    override def apply(requestHeader: RequestHeader): Accumulator[ByteString, Result] = {
//
//      val response: Accumulator[ByteString, Result] = requestHeader.contentType match {
//        case Some(MimeTypes.JSON) =>
//          val headerPairs: Seq[(String, String)] = (requestHeader.headers.toSimpleMap + xmlHeader).toSeq.map(t => t._1 -> t._2)
//          json2Xml transform nextFilter(requestHeader.copy(headers = Headers(headerPairs: _*)))
//
//        case _ | None =>
//          nextFilter(requestHeader)
//      }
//
//      response.mapFuture { result =>
//        // this returns xml if accept header is not specified
//        if (requestHeader.accepts(MimeTypes.XML)) {
//          Future.successful(result)
//        } else {
//          result.body.consumeData.map {
//            body =>
//              val xmlResponse = XMLUtil.loadString(body.decodeString(ByteString.UTF_8))
//              result.copy(body = HttpEntity.fromString(compact(render(toJson(xmlResponse))), "utf-8").asScala).withHeaders(CONTENT_TYPE -> JSON)
//          }
//        }
//      }
//
//    }
//  }
//
//  def jsonToXml(jsonData: JsValue) = internalJsonToXml(jsonData).child.head
//
//  private def internalJsonToXml(jsonData: JsValue): Node = {
//    val xmlResult = jsonData match {
//      case JsObject(fields) => {
//        fields.map {
//          case (key, value) => {
//            val result = Elem(null, key, Null, TopScope, false)
//            result.copy(null, key, Null, TopScope, false, internalJsonToXml(value).child)
//          }
//        }
//      }
//      case JsString(content) => Text(content)
//      case JsBoolean(bool) => Text(bool.toString)
//      case JsNumber(num) => Text(num.toString())
//      case JsArray(jsonArray) => jsonArray flatMap {
//        s => internalJsonToXml(s)
//      }
//      case JsNull => Text("null")
//      case j@JsUndefined() => <error>{Text(j.toString())}</error>
//    }
//    <result>{xmlResult}</result>
//  }
//
//  private val json2Xml = {
//    val arrayConcat: Enumeratee[ByteString, ByteString] = Enumeratee.grouped(Iteratee.consume[ByteString]())
//    val jsonParser: Enumeratee[ByteString, JsValue] = Enumeratee.map[ByteString]( value => Json.parse(value.utf8String))
//    val convertAndSerialize: Enumeratee[JsValue, ByteString] = Enumeratee.map[JsValue](j => ByteString(jsonToXml(j).toString()))
//    arrayConcat ><> jsonParser  ><> convertAndSerialize
//  }
//}

@Singleton
class AuthFilter @Inject()(implicit val mat: Materializer,
                           authenticator: RequestAuthenticator,
                           concurrencyUtils: ConcurrencyUtils) extends Filter {

  import concurrencyUtils.ec

  def apply(nextFilter: RequestHeader => Future[Result])(requestHeader: RequestHeader): Future[Result] = {
    Logger.info(s"SERVING Request ${requestHeader.uri}, ${requestHeader.method}")
    authenticator.validate(requestHeader) flatMap {
      case Right(headers) => nextFilter(headers)
      case Left(response) =>
        response match {
          case Forbidden =>
            Logger.warn(s"sending 403 for request: [${requestHeader.uri}], ${requestHeader.method}")
            forbidden
          case Unauthorized =>
            Logger.warn(s"sending 401 for request: [${requestHeader.uri}], ${requestHeader.method}")
            unauthorized
        }
    }
  }

  private val unauthorized = {
    Future.successful(Results.Unauthorized(<result><status>failure</status><message>Invalid Security Parameters</message></result>))
  }

  private val forbidden = {
    Future.successful(Results.Forbidden(<result><status>failure</status><message>Invalid Session Key/ Access Token</message></result>))
  }
}