package com.models

import com.utils.{DateUtil, JsonUtils, MySqlClient, StringUtils}
import play.api.libs.json.{JsValue, Json}

import java.sql.{ResultSet, Time}
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
                       createdOn: LocalDateTime,
                       status: String) //Possible values can be : Pending, Approved, rejected, Processed
{
  def toJson: JsValue = Json.obj(
    "aptId" -> aptId,
    "patId" -> patId,
    "docId" -> docId,
    "aptFromTime" -> aptFromTime,
    "aptToTime" -> aptToTime,
    "dateOfApt" -> dateOfApt,
    "createdOn" -> createdOn,
    "status" -> status
  )

  def getUpdatedAppointment(implicit json: JsValue): Appointment = {
    val patId = JsonUtils.get("patId").getOrElse(this.patId)
    val docId = JsonUtils.get("docId").getOrElse(this.docId)
    val aptFromTime = JsonUtils.get("aptFromTime").getOrElse(this.aptFromTime)
    val aptToTime = JsonUtils.get("aptToTime").getOrElse(this.aptToTime)
    val status = JsonUtils.get("status").getOrElse(this.status)
    val dateOfApt = JsonUtils.get("dateOfApt").map{ d =>
      val doapt = d.split("-").map(_.toInt)
      LocalDateTime.of(doapt(0), doapt(1), doapt(2),0,0)
    }.getOrElse(this.dateOfApt)

    this.copy(patId = patId, docId = docId, aptFromTime = aptFromTime, aptToTime = aptToTime, dateOfApt = dateOfApt, status = status)
  }
}

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
              Appointment(aptId, patId, docId, aptFromTime, aptToTime, dateOfAppointment, createdOn, "Pending")
            }
          }
        }
      }
    }
  }
}

@Singleton
class AppointmentManager @Inject()(mySqlClient: MySqlClient) {

  import com.utils.Java2Scala.ResultSetScala
  import com.utils.Java2Scala.asScala

  private val conn = mySqlClient.conn
  private val table = "appointment"

  private val insertPS = conn.prepareStatement(s"INSERT INTO $table(aptId, patId, docId, aptFromTime, aptToTime, dateOfApt, createdOn, status ) " +
    s"values(?, ?, ?, ?, ?, ?, ?, ?)")
  private val fetchByDocPS = conn.prepareStatement(s"select * from $table where docId = ?")
  private val fetchByPatPS = conn.prepareStatement(s"select * from $table where patId = ?")
  private val fetchByAptIdPS = conn.prepareStatement(s"select * from $table where aptId = ?")
  private val deletePS = conn.prepareStatement(s"delete from $table where aptId = ?")

  private def toAppointment(rs: ResultSet): Appointment = Appointment(
    rs.getString("aptId"),
    rs.getString("patId"),
    rs.getString("docId"),
    rs.getString("aptFromTime"),
    rs.getString("aptToTime"),
    rs.getTimestamp("dateOfApt").toLocalDateTime,
    rs.getTimestamp("createdOn").toLocalDateTime,
    rs.getString("status")
  )

  def create(apt: Appointment): Boolean = {
    insertPS.setString(1, apt.aptId)
    insertPS.setString(2, apt.patId)
    insertPS.setString(3, apt.docId)
    insertPS.setString(4, apt.aptFromTime)
    insertPS.setString(5, apt.aptToTime)
    insertPS.setDate(6, DateUtil.asDate(apt.dateOfApt))
    insertPS.setDate(7, DateUtil.asDate(apt.createdOn))
    insertPS.setString(8, apt.status)

    insertPS.executeUpdate() > 0
  }

  def fetchByPatId(patId: String): Seq[Appointment] = {
    fetchByPatPS.setString(1, patId)
    fetchByPatPS.executeQuery().mapTo(toAppointment)
  }

  def fetchByDocId(docId: String): Seq[Appointment] = {
    fetchByDocPS.setString(1, docId)
    fetchByDocPS.executeQuery().mapTo(toAppointment)
  }

  def delete(aptId: String): Boolean = {
    deletePS.setString(1, aptId)
    deletePS.executeUpdate() > 0
  }

  def fetch(aptId: String): Option[Appointment] = {
    fetchByAptIdPS.setString(1, aptId)
    fetchByAptIdPS.executeQuery().headOption.map(toAppointment)
  }

  def update(apt: Appointment): Boolean = {
    delete(apt.aptId)
    create(apt)
  }
}