package services

import javax.inject.Inject
import com.vividsolutions.jts.io.WKTReader
import database.MyPostgresDriver.api._
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile
import scala.concurrent.{ExecutionContext, Future}
import storages.{DriverStorage, DriverLocationStorage}
import models.{DriverModel, DriverLocationModel}

class DriverService @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  val driversQuery = TableQuery[DriverStorage]
  val driverLocationQuery = TableQuery[DriverLocationStorage]

  def listDrivers(): Future[Seq[DriverModel]] = {
    dbConfig.db.run(driversQuery.result)
  }
}