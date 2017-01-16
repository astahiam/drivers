package storages

import models.DriverLocationModel
import java.sql.Timestamp
import com.vividsolutions.jts.geom.Geometry
import slick.lifted.{ProvenShape, TableQuery}
import database.MyPostgresDriver.api._
import database.MyPostgresDriver.api.PgArrayPositionedResult


class DriverLocationStorage(tag: Tag) extends Table[DriverLocationModel](tag, "drivers_location") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def driverId = column[Long]("driver_id")
  def location = column[Geometry]("location")
  def createdAt = column[Timestamp]("created_at")
  def updatedAt = column[Timestamp]("updated_at")

  override def * =
    (id, driverId, location, createdAt, updatedAt) <> (DriverLocationModel.tupled, DriverLocationModel.unapply)
}

object DriverLocationStorage {
  val tableQuery = TableQuery[DriverLocationStorage]
}