package controllers

import play.api.mvc.Controller

class DriverController extends Controller {
  def update(driverId: Long, latitude: Double, longitude: Double) = TODO
  def search(latitude: Double, longitude: Double, radius: Long, limit: Int) = TODO
}