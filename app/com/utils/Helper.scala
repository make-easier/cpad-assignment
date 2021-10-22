package com.utils

import java.time.{LocalDateTime, ZoneId, ZoneOffset}
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.concurrent.Executor
import scala.xml._
import javax.inject.{Inject, Singleton}
import com.google.common.util.concurrent.{FutureCallback, Futures, ListenableFuture}
import org.apache.commons.codec.digest.DigestUtils
import play.api.Configuration

import java.sql.ResultSet
import scala.concurrent.{Future, Promise}
import scala.util.Random

case class AccessParams(generatedTimestamp: String, accessKey: String, accessSecret: String)

@Singleton
class Helper @Inject()(configuration: Configuration) {

  def trimXML = scala.xml.Utility.trim _

  val maxRetries = configuration.getInt("MAX_SESSION_RETRY")

  def successResult = {
    trimXML(
      <status>Success</status>
    )
  }

}


object XMLHelper {
  def getTextIfExists(xml: Node, fieldName: String): Option[String] = {
    (xml \\ fieldName).headOption.map { node => node.text }
  }

  def getText(xml: Node, elemName: String): String = {
    (xml \\ elemName).head.text
  }
}

object Java2Scala {
  implicit def asScala[T](listenableFuture: ListenableFuture[T])(implicit ex: Executor): Future[T] = {
    val p = Promise[T]
    Futures.addCallback(listenableFuture, new FutureCallback[T]() {
      def onSuccess(result: T) = p success result
      def onFailure(t: Throwable) = p failure t
    }, ex)
    p future
  }

  implicit def results[T](resultSet: ResultSet)(f: ResultSet => T) = {
    new Iterator[T] {
      def hasNext = resultSet.next()
      def next() = f(resultSet)
    }.toSeq
  }

  implicit def asScala(resultSet: ResultSet): Seq[ResultSet] = {
    new Iterator[ResultSet] {
      def hasNext = resultSet.next()
      def next() = resultSet
    }.toSeq
  }

  implicit class ResultSetScala(resultSet: ResultSet) {
    def mapTo[T](f:ResultSet => T): Seq[T] = {
      new Iterator[T] {
        def hasNext = resultSet.next()
        def next() = f(resultSet)
      }.toSeq
    }
  }

}

//For reference
object Implicits {

  implicit class ResultSetScala(resultSet: ResultSet) {

    def asScala: Seq[ResultSet] = {
      new Iterator[ResultSet] {
        def hasNext = resultSet.next()
        def next() = resultSet
      }.toSeq
    }
  }

  implicit class MyConversion[T](itr: Seq[T]) {
    def asSardar(f: T => String): Seq[String] = itr.map(f)

  }
}

@Singleton
class SecureStringGenerator {
  val stringSize:Int = 32
  def getSha256String: String = {
    DigestUtils.sha256Hex(Random.alphanumeric.take(stringSize).mkString + LocalDateTime.now)
  }

  def getMd5String: String ={
    DigestUtils.md5Hex(Random.alphanumeric.take(stringSize).mkString + LocalDateTime.now)
  }
}

object DateUtil {
  import java.sql.Date
  def getDates(from: LocalDateTime, to: LocalDateTime): Seq[LocalDateTime] = {
    val numberOfDays = ChronoUnit.DAYS.between(from, to).toInt

    (0 to numberOfDays).map(n => from.plusDays(n))
  }

  def asLocalDateTime(time: Date, zoneId: ZoneId = ZoneOffset.UTC): LocalDateTime = {
    time.toInstant.atZone(zoneId).toLocalDateTime
  }

  def asDate(time: LocalDateTime): Date = {
    Date.valueOf(time.toLocalDate)
  }


}