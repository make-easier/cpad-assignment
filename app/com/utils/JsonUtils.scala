package com.utils

import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.JsValue.jsValueToJsLookup

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

object JsonUtils {
  def get(key: String, filterEmpty: Boolean = true)(implicit json: JsValue): Option[String] = {
    val result = (json \\ key).headOption.map(v => v.as[String].trim)
    filterEmpty match {
      case true => result.filter(_.nonEmpty)
      case false => result
    }
  }

  def getAll(key: String)(implicit json: JsValue): Seq[String] =
    (json \\ key).map(n => n.as[String].trim).filter(_.nonEmpty)

  def getLongIfExists(fieldName: String)(implicit json: JsValue): Option[Long] =
    get(fieldName).flatMap { value =>
      convertTo(Try(value.toLong))
    }

  private def convertTo[A](conversion: Try[A]): Option[A] = conversion match {
    case Success(value) => Some(value)
    case Failure(ex) => Logger.warn(s"Can't be converted to required type ${ex.getMessage}")
      None
  }

}
