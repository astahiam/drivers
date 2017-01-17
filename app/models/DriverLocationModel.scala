package models

import java.sql.Timestamp

import com.vividsolutions.jts.geom.Geometry


case class DriverLocationModel
(
  id: Long,
  driverId: Long,
  location: Geometry,
  created_at: Timestamp,
  updated_at: Timestamp
)