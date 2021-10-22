package com.utils

import play.api.Logger

import scala.util.{Failure, Success, Try}
import scala.xml.NodeSeq

object XMLUtils {
  def get(key: String, filterEmpty: Boolean = true)(implicit xml: NodeSeq): Option[String] = {
    val result = (xml \\ key).headOption.map(_.text.trim)
    filterEmpty match {
      case true => result.filter(_.nonEmpty)
      case false => result
    }
  }

  def getAll(key: String)(implicit xml: NodeSeq): Seq[String] =
    (xml \\ key).map(n => n.text.trim).filter(_.nonEmpty)

  def getLongIfExists(fieldName: String)(implicit xml: NodeSeq): Option[Long] =
    get(fieldName).flatMap { value =>
      convertTo(Try(value.toLong))
    }

  private def convertTo[A](conversion: Try[A]): Option[A] = conversion match {
    case Success(value) => Some(value)
    case Failure(ex) => Logger.warn(s"Can't be converted to required type ${ex.getMessage}")
      None
  }
}
