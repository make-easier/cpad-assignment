package com.controllers

import com.models.{Prescription, PrescriptionManager}
import com.utils.ConcurrencyUtils
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{BaseController, ControllerComponents}

import java.io.File
import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.reflect.io
import scala.reflect.io.File

/**
 * Created by S Suryakant on 21/10/21.
 */
@Singleton
class Prescription @Inject()(presManager: PrescriptionManager,
                             val controllerComponents: ControllerComponents,
                             concurrencyUtils: ConcurrencyUtils) extends BaseController {

  import concurrencyUtils.ec

//  private def saveFile(file: File) = {
//    ???
//  }

  def createPres() = Action.async(parse.multipartFormData) { request =>
    val formData = request.body.dataParts

    def get(key: String): Option[String] = formData.get(key).flatMap(r => r.headOption).filter(_.nonEmpty)

    val patId = get("patId")
    val prescribedBy = get("prescribedBy")
    val docId = get("docId")
    val presName = get("presName")

    if (patId.isEmpty || prescribedBy.isEmpty || docId.isEmpty || presName.isEmpty) {
      Logger.warn(s"Required fields are missing in request body $patId, $prescribedBy, $docId, $presName")
      Future(BadRequest(Json.obj("status" -> "Failure", "message" -> "Required fields are missing in request")))
    } else {
      if (request.body.files.headOption != None) {
        val fileTempPath = request.body.files.head.ref.file
        val prescriptionDoc = fileTempPath.getAbsoluteFile
        Logger.debug("file received :" + request.body.files.head.filename)
        val prescription = Prescription(patId.get, prescribedBy.get, docId.get, presName.get, prescriptionDoc.getAbsolutePath)

        presManager.create(prescription) match {
          case true => Logger.info(s"Prescription Crated with $prescription")
            Future(Ok(Json.obj("status" -> "Success", "message" -> "Prescription Crated")))
          case false => Logger.warn(s"Prescription Creation failed with $prescription")
            Future(BadRequest(Json.obj("status" -> "Failure", "message" -> "Prescription Creation failed")))
        }
      } else Future(BadRequest(Json.obj("status" -> "Failure", "message" -> "No file is attached!")))
    }
  }

  def fetchPres(patId: String) = Action.async { request =>
    val response = Json.obj("prescriptions" ->
      presManager.fetch2(patId).map(_.toJson())
    )
    println(s"Response: $response")
    presManager.test
    Future(Ok(response))
  }

  def updatePres(presId: String) = Action.async(parse.multipartFormData) { request =>
    Future(Ok(""))
  }
}
