package com.controllers

import com.models.{Appointment, AppointmentManager}
import com.utils.ConcurrencyUtils
import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{BaseController, ControllerComponents}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

/**
 * Created by S Suryakant on 18/10/21.
 */
@Singleton
class Appointment @Inject()(concurrencyUtils: ConcurrencyUtils,
                            val controllerComponents: ControllerComponents,
                            aptManager: AppointmentManager) extends BaseController {

  import concurrencyUtils.ec

  def create() = Action.async(parse.json) { request =>
    Logger.info(s"")
    val json = request.body
    Appointment(json).map { appointment =>
      aptManager.create(appointment) match {
        case true => Logger.info(s"Appointment booked for $appointment")
          Future(Ok(Json.obj("status" -> "Success", "message" -> "Appointment Booked")))
        case false => Logger.warn(s"Some error occurred while creating appointment $appointment")
          Future(BadRequest(Json.obj("status" -> "Failure", "message" -> "Appointment Booking failed")))
      }
    }.getOrElse {
      Logger.warn(s"Invalid Request body")
      Future(BadRequest(Json.obj("status" -> "Failure", "message" -> "Invalid request body")))
    }
  }
}
