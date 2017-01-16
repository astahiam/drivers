package storages

import models.DriverModel
import java.sql.Timestamp
import slick.lifted.TableQuery
import database.MyPostgresDriver.api._

class DriverStorage(tag: Tag) extends Table[DriverModel](tag, "drivers") {
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def name = column[String]("name")
  def vehicle = column[String]("vehicle")
  def createdAt = column[Timestamp]("created_at")
  def updatedAt = column[Timestamp]("updated_at")

  override def * =
    (id, name, vehicle, createdAt, updatedAt) <> (DriverModel.tupled, DriverModel.unapply)
}

object DriverStorage {
  val tableQuery = TableQuery[DriverStorage]
}