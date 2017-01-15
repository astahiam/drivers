package controllers

import play.api.mvc.{Controller, Action}
import play.api.libs.json.Json

class DriverController extends Controller {
  def update(driverId: Long, latitude: Double, longitude: Double) = TODO
  def search(latitude: Double, longitude: Double, radius: Long, limit: Int) = TODO

  private def isValidLatitude(latitude: Double) = {
    if (latitude <= 90.0 && latitude >= -90.0) {
      true
    } else {
      false
    }
  }
}