package models

import com.vividsolutions.jts.geom.{GeometryFactory, PrecisionModel}
import com.vividsolutions.jts.io.WKTReader

case class Location(latitude: Double, longitude: Double) {
  def point = Location.wktReader.read(s"POINT($latitude $longitude)")
}

object Location {
  private val SRID = 4326
  private val wktReader = new WKTReader(new GeometryFactory(new PrecisionModel(), SRID))
}