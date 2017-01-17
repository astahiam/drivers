package services

import javax.inject.Inject
import java.util.UUID
import com.vividsolutions.jts.geom.Geometry
import database.MyPostgresDriver.api._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import storages.{DriverStorage, DriverLocationStorage}
import models.{DriverModel, DriverLocationModel, Location}

class DriverService @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  val driversQuery = TableQuery[DriverStorage]
  val driverLocationQuery = TableQuery[DriverLocationStorage]

  def listDrivers(): Future[Seq[DriverModel]] = {
    dbConfig.db.run(driversQuery.result)
  }

  def updateDriverLocation(driverId: Long, latitude: Double, longitude: Double): Future[Int] = {
    val point = Location(latitude, longitude).point
    dbConfig.db.run(driverLocationQuery.filter(_.driverId === driverId).map(_.location).update(point))
  }

  def searchDrivers(latitude: Double, longitude: Double, radius: Double, limit: Int): Future[Seq[(Long, Double, Double, Double)]] = {
    val query = searchDriversQuery(latitude, longitude, radius, limit)
    dbConfig.db.run(query)
  }

  private def searchDriversQuery(latitude: Double, longitude: Double, radius: Double, limit: Int) = {
    sql"""
       SELECT * FROM
       (SELECT drivers_location.driver_id,
              ST_X(ST_TRANSFORM(drivers_location.location, 4326)) as latitude,
              ST_Y(ST_TRANSFORM(drivers_location.location, 4326)) as longitude,
              earth_distance(ll_to_earth($latitude, $longitude),
              ll_to_earth(ST_X(ST_TRANSFORM(drivers_location.location, 4326)),
              ST_Y(ST_TRANSFORM(drivers_location.location, 4326)))) as distance
       FROM drivers_location ORDER BY distance ASC) A
       WHERE distance < $radius limit $limit
    """.as[(Long, Double, Double, Double)]
  }
}