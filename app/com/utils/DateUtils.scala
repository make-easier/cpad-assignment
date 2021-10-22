package com.utils

import org.joda.time.{DateTime, Months}

import java.text.SimpleDateFormat
import java.time.temporal.ChronoUnit
import java.time.{LocalDateTime, ZoneId, ZoneOffset}
import java.util.{Date, TimeZone}
import javax.inject.Singleton
import scala.util.Try

/**
 * Created by S Suryakant on 18/10/21.
 */
@Singleton
class DateUtils {

  private val defaultZoneOffset = ZoneOffset.UTC
  private val defaultZoneId = ZoneId.systemDefault()

  /**
    * java.util.Date is supported in the cassandra driver , conversion from LocalDateTime is needed
    * epoch time in case of Date is in milliseconds
    * epoch time in case of LocalDateTime is in Seconds
    * so conversion from seconds to milliseconds is required to convert from LocalDateTime epoch time
    * to java.util.Date epoch time
    *
    * @param localDateTime LocalDateTime
    * @return java.util.Date
    */

  def asDate(localDateTime: LocalDateTime, zoneOffset: ZoneOffset = defaultZoneOffset): Date = {
    new Date(localDateTime.toEpochSecond(zoneOffset) * 1000)
  }

  def asLocalDateTime(date: Date, zoneId: ZoneId = defaultZoneId): LocalDateTime = {
    LocalDateTime.ofInstant(date.toInstant, zoneId)
  }

  def lapsedTimeInSeconds(time: LocalDateTime, zoneOffset: ZoneOffset = defaultZoneOffset): Long = {
    LocalDateTime.now.toEpochSecond(zoneOffset) - time.toEpochSecond(zoneOffset)
  }

  def formatWithZone(time: Date, formatStr: String, zone: Option[TimeZone]): String = {
    val dateFormatter = new SimpleDateFormat(formatStr)
    zone.foreach(dateFormatter.setTimeZone)
    dateFormatter.format(time)
  }

  def getTimeZone(zoneStr: String): Option[TimeZone] = {
    Try(TimeZone.getTimeZone(zoneStr))
      .toOption
  }

  def getMonthYear: String = {
    val time = LocalDateTime.now()
    getMonthYear(time)
  }

  def getMonthYear(time: LocalDateTime): String = {
    s"${time.getMonthValue}${time.getYear}"
  }

  def getMonthYearAsInt: Int = {
    getMonthYear.toInt
  }

  def fromDateTimeToLocalDateTime(dateTime: DateTime): LocalDateTime = {
    LocalDateTime.from(asLocalDateTime(dateTime.toDate))
  }

  def monthsBetween(from: DateTime, till: DateTime): Seq[Int] = {
    val monthCount = Months.monthsBetween(
      from.withTimeAtStartOfDay().withDayOfMonth(1),
      till.withDayOfMonth(1)).getMonths
    val months = for (m <- 1 to monthCount) yield getMonthAndYear(from.plusMonths(m))
    (getMonthAndYear(from) +: months).toSeq
  }

  def monthsBetween(from: LocalDateTime, till: LocalDateTime): Seq[Int] = {
    if(from.isBefore(till)) {
      val count = ChronoUnit.MONTHS.between(from.withDayOfMonth(1), till).toInt
      for (i <- 0 to count) yield {
        getMonthAndYear(from.plusMonths(i))
      }
    } else Nil
  }

  def getMonthAndYear(time: LocalDateTime): Int = {
    s"""${time.getMonth.getValue}${time.getYear}""".toInt
  }

  private def getMonthAndYear(date: DateTime): Int =
    s"${date.getMonthOfYear}${date.getYear}".toInt

}
