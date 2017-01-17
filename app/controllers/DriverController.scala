package controllers

import java.util.UUID

import services.DriverService
import javax.inject.Inject
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import responses._
import models.{DriverLocationModel, DriverModel}

import play.api.Logger
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

class DriverController @Inject()(val driverService: DriverService)(implicit val ec: ExecutionContext) extends Controller {
  def update(driverId: Long, latitude: Double, longitude: Double) = Action.async {
    //isValidLatitude(latitude) match {
      //case true => {
        driverService.updateDriverLocation(driverId, latitude, longitude).map(result =>
          result match {
            case 0 => BadRequest(Json.toJson(Json.obj("error" -> "Update Driver's Location Failed")))
            case _ => NoContent
          }
        )
      //}
     // case false => BadRequest(Json.toJson(Json.obj("error" -> "Latitude should be between +/- 90")))
    //}
  }
  def search(latitude: Double, longitude: Double, radius: Double, limit: Int) = Action.async {
    //isValidLatitude(latitude) match {
    //  case true =>
        driverService.searchDrivers(latitude, longitude, radius, limit).map ({driversResult =>
          Ok(ResponseWrapper(
            Json.toJson(Json.obj("name" -> "driver's location")),
            Json.toJson(Json.obj("id" -> driversResult.map(_._1), "latitude" -> driversResult.map(_._2), "longitude" -> driversResult.map(_._3), "distance" -> driversResult.map(_._4)))
          ))
        })
   //   case false => BadRequest(Json.toJson(Json.obj("error" -> "Latitude should be between +/- 90")))
   // }

  }
  def list() = Action.async {
    driverService.listDrivers.map({ drivers =>
        Ok(ResponseWrapper(
          Json.toJson(Json.obj("name" -> "drivers")),
          Json.toJson(drivers.map(driver => driver.name))
          )
        )
    })
  }

  private def isValidLatitude(latitude: Double) = {
    if (latitude <= 90.0 && latitude >= -90.0) {
      true
    } else {
      false
    }
  }
}