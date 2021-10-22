package com.controllers

import com.utils.{ConcurrencyUtils, MySqlClient}
import play.api.mvc._

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}
import scala.xml.Node

/**
  * Created by S Suryakant on 17/07/21.
  */
@Singleton
class Application @Inject()(mySqlClient: MySqlClient,
                            concurrencyUtils: ConcurrencyUtils) extends Controller {

  import concurrencyUtils.ec
  private val result = Future.successful( Ok("""{"response": {"status": "Success", "data:" "pong"}}"""))

  val conn = mySqlClient.conn
  def ping = Action.async { request =>
//    println("getting INT: "+getIntIfExists(<INT>100s</INT>, "INT"))
//    println("getting UUID: "+getUUIDIfExists(<uuidVal>q80f31c3-e0e7-46e8-9855-f87c55e9e2f8</uuidVal>, "uuidVal"))

    result
  }

  def getTextIfExists(xml: Node, fieldName: String): Option[String] = {
    (xml \\ fieldName).headOption.map { node => node.text.trim}.filter(_.nonEmpty)
  }

  def getIntIfExists(xml: Node, fieldName: String): Option[Int] =
    getTextIfExists(xml, fieldName).flatMap { value =>
      convertTo(Try(value.toInt))
    }

  def getUUIDIfExists(xml: Node, fieldName: String): Option[UUID] =
    getTextIfExists(xml, fieldName).flatMap { value =>
      convertTo(Try(UUID.fromString(value)))
    }

  private def convertTo[A](condition: Try[A]): Option[A] = condition match {
    case Success(value) => Some(value)
    case Failure(ex) => println(s"Can't be converted to required type ${ex.getMessage}")
      None
  }
}