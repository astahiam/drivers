package models

import java.sql.Timestamp

case class DriverModel
(
  id: Long,
  name: String,
  vehicle: String,
  created_at: Timestamp,
  updated_at: Timestamp
)