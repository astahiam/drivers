package controllers

import services.DriverService
import javax.inject.Inject
import responses.ResponseWrapper
import scala.concurrent.ExecutionContext
import play.api.libs.json.{Json, JsValue}
import play.api.mvc.{Action,Controller,Result}


class DriverController @Inject()(val driverService: DriverService)(implicit val ec: ExecutionContext) extends Controller {
  def update(driverId: Long, latitude: Double, longitude: Double) = TODO
  def search(latitude: Double, longitude: Double, radius: Long, limit: Int) = TODO
  def list() = Action.async {
    driverService.listDrivers.map({ drivers =>
        Ok(ResponseWrapper(Json.toJson(Json.obj("name" -> "drivers")), Json.toJson(drivers.map(_.name))))
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