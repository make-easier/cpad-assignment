package com.models

import com.utils.{DateUtil, JsonUtils, MySqlClient, StringUtils}
import play.api.libs.json.JsValue

import java.sql.Time
import java.time.{LocalDateTime, LocalTime}
import javax.inject.{Inject, Singleton}

/**
 * Created by S Suryakant on 18/10/21.
 */
case class Appointment(aptId: String,
                       patId: String,
                       docId: String,
                       aptFromTime: String,
                       aptToTime: String,
                       dateOfApt: LocalDateTime,
                       createdOn: LocalDateTime)

object Appointment {
  def apply(implicit json: JsValue): Option[Appointment] = {
    JsonUtils.get("patId").flatMap { patId =>
      JsonUtils.get("docId").flatMap{ docId =>
        JsonUtils.get("aptFromTime").flatMap { aptFromTime =>
          JsonUtils.get("aptToTime").flatMap { aptToTime =>
            JsonUtils.get("dateOfApt").map { dateOfApt => //example of date as 2021-10-18
              val doapt = dateOfApt.split("-").map(_.toInt)
              val dateOfAppointment = LocalDateTime.of(doapt(0), doapt(1), doapt(2),0,0)
              val createdOn = LocalDateTime.now()
              val aptId = s"$patId-$docId-${StringUtils.getAlphaNumericString(5)}"
              Appointment(aptId, patId, docId, aptFromTime, aptToTime, dateOfAppointment, createdOn)
            }
          }
        }
      }
    }
  }
}

@Singleton
class AppointmentManager @Inject()(mySqlClient: MySqlClient) {
  private val conn = mySqlClient.conn
  private val table = "appointment"

  private val insertPS = conn.prepareStatement(s"INSERT INTO $table(aptId, patId, docId, aptFromTime, aptToTime, dateOfApt, createdOn) " +
    s"values(?, ?, ?, ?, ?, ?, ?)")
  private val fetchAllPS = conn.prepareStatement(s"select * from $table")
  private val fetchByPatPS = conn.prepareStatement(s"select * from $table where patId = ?")

  def create(apt: Appointment): Boolean = {
    insertPS.setString(1, apt.aptId)
    insertPS.setString(2, apt.patId)
    insertPS.setString(3, apt.docId)
    insertPS.setTime(4, new Time(1,1,1))
    insertPS.setTime(5, new Time(1121233))//apt.aptToTime)
    insertPS.setDate(6, DateUtil.asDate(apt.dateOfApt))
    insertPS.setDate(7, DateUtil.asDate(apt.createdOn))

    insertPS.executeUpdate() > 0
  }

}