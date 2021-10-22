package com.models

import com.utils.{DateUtil, JsonUtils, MySqlClient, StringUtils}
import play.api.libs.json.{JsValue, Json}

import java.sql.{Date, ResultSet}
import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}

/**
 * Created by S Suryakant on 18/10/21.
 */
case class Prescription(presId: String,
                        patId: String,
                        prescribedBy: String,
                        docId: String,
                        presName: String,
                        presUrl: String,
                        createdOn: LocalDateTime,
                        lastModifiedOn: LocalDateTime) {

  def toJson(): JsValue = Json.obj(
    "presId" -> presId,
    "patId" -> patId,
    "prescribedBy" -> prescribedBy,
    "docId" -> docId,
    "presName" -> presName,
    "presUrl" -> presUrl,
    "createdOn" -> createdOn,
    "lastModifiedOn" -> lastModifiedOn
  )
}

object Prescription {
  def apply(implicit request: JsValue): Option[Prescription] = {
    JsonUtils.get("patId").flatMap { patId =>
      JsonUtils.get("prescribedBy").flatMap { prescribedBy =>
        JsonUtils.get("docId").flatMap { docId =>
          JsonUtils.get("presName").map { presName =>
            val date = LocalDateTime.now()
            val presUrl = "needToUpload"
            val presId = patId+StringUtils.getAlphaNumericString(5)
            Prescription(presId, patId, prescribedBy, docId, presName, presUrl, date, date)
          }
        }
      }
    }
  }

}

@Singleton
class PrescriptionManager @Inject()(mySqlClient: MySqlClient) {
  private val conn = mySqlClient.conn
  private val table = "Prescription"

  private val updatePS = conn.prepareStatement(s"update prescription set presUrl = ?, lastModifiedOn = ? where presId= ?")
  private val fetchPS = conn.prepareStatement(s"select * from $table where patId = ?")
  private val createPS = conn.prepareStatement(s"INSERT INTO $table(presId, patId, prescribedBy, docId, presName, presUrl, createdOn, lastModifiedOn) " +
    s"values(?, ?, ?, ?, ?, ?, ?, ?)")

  private def toPrescription(rs: ResultSet): Prescription = Prescription(
    rs.getString("presId"),
    rs.getString("patId"),
    rs.getString("prescribedBy"),
    rs.getString("docId"),
    rs.getString("presName"),
    rs.getString("presUrl"),
    rs.getTimestamp("createdOn").toLocalDateTime,
    rs.getTimestamp("lastModifiedOn").toLocalDateTime
  )

  private def rs2prescription(rs: ResultSet): Seq[Prescription] = {
    var prescriptions: Seq[Prescription] = Seq.empty[Prescription]
    while (rs.next()) {
      val p = toPrescription(rs)
      prescriptions ++= Seq(p)
    }
    println(s"SIZE : ${prescriptions.size}")
    prescriptions
  }

  def create(p: Prescription): Boolean = {
    createPS.setString(1, p.presId)
    createPS.setString(2, p.patId)
    createPS.setString(3, p.prescribedBy)
    createPS.setString(4, p.docId)
    createPS.setString(5, p.presName)
    createPS.setString(6, p.presUrl)
    createPS.setDate(7, DateUtil.asDate(p.createdOn))
    createPS.setDate(8, DateUtil.asDate(p.lastModifiedOn))

    createPS.executeUpdate() > 0
  }

  import com.utils.Java2Scala.results
  import com.utils.Java2Scala.asScala
  import com.utils.Java2Scala.ResultSetScala
//  import com.utils.Implicits.ResultSetScala

  def fetch2(patId: String): Seq[Prescription] = {
    fetchPS.setString(1, patId)

//    fetchPS.executeQuery().map(toPrescription)
    fetchPS.executeQuery().mapTo(toPrescription)
//    val resultSet = results[Prescription](fetchPS.executeQuery())(toPrescription)
//    resultSet
  }

  def test = {
    val list = List("ab", "bc", "Dc")
    import com.utils.Implicits.MyConversion

    println(list.asSardar(_.toUpperCase))
    println(list.asSardar(_.toLowerCase))
  }

  def fetch(patId: String): Seq[Prescription] = {
    fetchPS.setString(1, patId)

    val resultSet = fetchPS.executeQuery()
    rs2prescription(resultSet)
  }

  def update(presId: String, presUrl: String): Boolean = {
    val date = DateUtil.asDate(LocalDateTime.now())
    updatePS.setString(1, presUrl)
    updatePS.setDate(2, date)
    updatePS.setString(3, presId)

    updatePS.executeUpdate() > 0
  }

}
