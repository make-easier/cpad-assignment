package com.controllers

import com.models.{Prescription, PrescriptionManager}
import com.utils.ConcurrencyUtils
import play.api.Logger
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc.{BaseController, ControllerComponents}

import java.time.LocalDateTime
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

/**
 * Created by S Suryakant on 21/10/21.
 */
@Singleton
class Prescription @Inject()(presManager: PrescriptionManager,
                             val controllerComponents: ControllerComponents,
                             concurrencyUtils: ConcurrencyUtils) extends BaseController {

  import concurrencyUtils.ec

  def createPres() = Action.async(parse.json) { request =>
    val body = request.body
    Prescription(body).map { prescription =>
      presManager.create(prescription) match {
        case true => Logger.info(s"Prescription Crated with $prescription")
          Future(Ok(Json.obj("status" -> "Success", "message" -> "Prescription Crated")))
        case false => Logger.warn(s"Prescription Creation failed with $prescription")
          Future(BadRequest(Json.obj("status" -> "Failure", "message" -> "Prescription Creation failed")))
      }
    }.getOrElse {
      Logger.warn(s"Invalid Request body")
      Future(BadRequest(Json.obj("status" -> "Failure", "message" -> "Invalid request body")))
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
