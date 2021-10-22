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

  def fetchByPatient(patId: String) = Action.async { request =>
    val response = Json.obj("appointments" -> aptManager.fetchByPatId(patId).map(_.toJson))
    Logger.info(s"Response of fetchByPatient for patId: $patId is \n $response")
    Future(Ok(response))
  }

  def fetchByDoctors(docId: String) = Action.async { request =>
    val response = Json.obj("appointments" -> aptManager.fetchByDocId(docId).map(_.toJson))
    Logger.info(s"Response of fetchByDoctors for docId: $docId is \n $response")
    Future(Ok(response))
  }

  def cancel(aptId: String) = Action.async { request =>
    Logger.info(s"Going to delete appointment $aptId")
    aptManager.fetch(aptId).map { appointment =>
      if (aptManager.delete(aptId)) {
        Logger.info(s"Appointment with id $aptId got deleted successfully")
        Future(Ok(s"""{"status": "Success", "message": "Appointment Cancelled"}"""))
      } else {
        Logger.warn(s"Error occurred while deleting Appointment with id $aptId")
        Future(Unauthorized(s"""{"status": "Failure", "message": "Appointment Cancellation failed"}"""))
      }
    }.getOrElse {
      Logger.warn(s"There is no appointment with id $aptId, so can't be deleted")
      Future(NotFound(Json.obj("status" -> "Failure", "message" -> "Invalid appointmentId")))
    }
  }

  def update(aptId: String) = Action.async(parse.json) { request =>
    aptManager.fetch(aptId).map { appointment =>
      val updatedAppointment = appointment.getUpdatedAppointment(request.body)
      aptManager.update(updatedAppointment) match {
        case true => Logger.info(s"Updated Appointment is $updatedAppointment")
          Future(Ok(Json.obj("status" -> "Success", "message" -> "Appointment updated successfully")))
        case false => Logger.warn(s"Failed to update appointment with id: $aptId")
          Future(InternalServerError(Json.obj("status" -> "Failure", "message" -> "Failed to update appointment")))
      }
    }.getOrElse {
      Logger.warn(s"There is no appointment with id $aptId, so can't be updated")
      Future(NotFound(Json.obj("status" -> "Failure", "message" -> "Invalid appointmentId")))
    }
  }

}
