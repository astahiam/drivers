/*
Copyright (c) 2013, Minglei Tu (tmlneu@gmail.com)
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
 */

package database

import java.sql.{PreparedStatement, ResultSet}

import com.github.tminglei.slickpg.utils
import com.vividsolutions.jts.geom.{LinearRing, MultiLineString, MultiPoint, MultiPolygon, _}
import com.vividsolutions.jts.io.{WKBWriter, WKTReader, WKTWriter, _}
import slick.ast.FieldSymbol
import slick.driver.PostgresDriver
import slick.jdbc.{JdbcType, PositionedParameters, PositionedResult, SetParameter}
import scala.reflect.ClassTag

trait PgPostGISSupport extends geom.CustomPostgisExtenstions { driver: PostgresDriver =>
  import driver.api._

  trait PostGISAssistants extends BasePostGISAssistants[Geometry, Point, LineString, Polygon, GeometryCollection]

  trait PostGISImplicits {
    implicit val geometryTypeMapper: JdbcType[Geometry] = new GeometryJdbcType[Geometry]
    implicit val pointTypeMapper: JdbcType[Point] = new GeometryJdbcType[Point]
    implicit val polygonTypeMapper: JdbcType[Polygon] = new GeometryJdbcType[Polygon]
    implicit val lineStringTypeMapper: JdbcType[LineString] = new GeometryJdbcType[LineString]
    implicit val linearRingTypeMapper: JdbcType[LinearRing] = new GeometryJdbcType[LinearRing]
    implicit val geometryCollectionTypeMapper: JdbcType[GeometryCollection] = new GeometryJdbcType[GeometryCollection]
    implicit val multiPointTypeMapper: JdbcType[MultiPoint] = new GeometryJdbcType[MultiPoint]
    implicit val multiPolygonTypeMapper: JdbcType[MultiPolygon] = new GeometryJdbcType[MultiPolygon]
    implicit val multiLineStringTypeMapper: JdbcType[MultiLineString] = new GeometryJdbcType[MultiLineString]

    ///
    implicit def geometryColumnExtensionMethods[G1 <: Geometry](c: Rep[G1]) =
      new GeometryColumnExtensionMethods[Geometry, Point, LineString, Polygon, GeometryCollection, G1, G1](c)
    implicit def geometryOptionColumnExtensionMethods[G1 <: Geometry](c: Rep[Option[G1]]) =
      new GeometryColumnExtensionMethods[Geometry, Point, LineString, Polygon, GeometryCollection, G1, Option[G1]](c)
  }

  trait PostGISPlainImplicits {
    import PgPostGISSupportUtils._
    import utils.PlainSQLUtils._

    implicit class PostGISPositionedResult(r: PositionedResult) {
      def nextGeometry[T <: Geometry](): T = nextGeometryOption().getOrElse(null.asInstanceOf[T])
      def nextGeometryOption[T <: Geometry](): Option[T] = r.nextStringOption().map(fromLiteral[T])
    }

    ////////////////////////////////////////////////////////////////////////////////
    implicit val getGeometry = mkGetResult(_.nextGeometry[Geometry]())
    implicit val getGeometryOption = mkGetResult(_.nextGeometryOption[Geometry]())

    implicit object SetGeometry extends SetParameter[Geometry] {
      def apply(v: Geometry, pp: PositionedParameters) = setGeometry(Option(v), pp)
    }
    implicit object SetGeometryOption extends SetParameter[Option[Geometry]] {
      def apply(v: Option[Geometry], pp: PositionedParameters) = setGeometry(v, pp)
    }

    ///
    private def setGeometry[T <: Geometry](v: Option[T], p: PositionedParameters) = v match {
      case Some(v) => p.setBytes(toBytes(v))
      case None    => p.setNull(java.sql.Types.OTHER)
    }
  }

  //////////////////////// geometry jdbc type ///////////
  class GeometryJdbcType[T <: Geometry](implicit override val classTag: ClassTag[T]) extends DriverJdbcType[T] {
    import PgPostGISSupportUtils._

    override def sqlType: Int = java.sql.Types.OTHER

    override def sqlTypeName(sym: Option[FieldSymbol]): String = "geometry"

    override def getValue(r: ResultSet, idx: Int): T = {
      val value = r.getString(idx)
      if (r.wasNull) null.asInstanceOf[T] else fromLiteral[T](value)
    }

    override def setValue(v: T, p: PreparedStatement, idx: Int): Unit = p.setBytes(idx, toBytes(v))

    override def updateValue(v: T, r: ResultSet, idx: Int): Unit = r.updateBytes(idx, toBytes(v))

    override def hasLiteralForm: Boolean = false

    override def valueToSQLLiteral(v: T) = if(v eq null) "NULL" else s"'${toLiteral(v)}'"
  }
}

object PgPostGISSupportUtils {
  private val wktWriterHolder = new ThreadLocal[WKTWriter]
  private val wktReaderHolder = new ThreadLocal[WKTReader]
  private val wkbWriterHolder = new ThreadLocal[WKBWriter]
  private val wkb3DWriterHolder = new ThreadLocal[WKBWriter]
  private val wkbReaderHolder = new ThreadLocal[WKBReader]

  def toLiteral(geom: Geometry): String = {
    if (wktWriterHolder.get == null) wktWriterHolder.set(new WKTWriter())
    wktWriterHolder.get.write(geom)
  }
  def fromLiteral[T](value: String): T = {
    if (wktReaderHolder.get == null) wktReaderHolder.set(new WKTReader())
    splitRSIDAndWKT(value) match {
      case (srid, wkt) => {
        val geom =
          if (wkt.startsWith("00") || wkt.startsWith("01"))
            fromBytes(WKBReader.hexToBytes(wkt))
          else wktReaderHolder.get.read(wkt)

        if (srid != -1) geom.setSRID(srid)
        geom.asInstanceOf[T]
      }
    }
  }

  def toBytes(geom: Geometry): Array[Byte] = {
    if (geom != null && geom.getCoordinate != null && !(java.lang.Double.isNaN(geom.getCoordinate.z))) {
      if (wkb3DWriterHolder.get == null) wkb3DWriterHolder.set(new WKBWriter(3, true))
      wkb3DWriterHolder.get.write(geom)
    } else {
      if (wkbWriterHolder.get == null) wkbWriterHolder.set(new WKBWriter(2, true))
      wkbWriterHolder.get.write(geom)
    }
  }
  private def fromBytes[T](bytes: Array[Byte]): T = {
    if (wkbReaderHolder.get == null) wkbReaderHolder.set(new WKBReader())
    wkbReaderHolder.get.read(bytes).asInstanceOf[T]
  }

  /** copy from [[org.postgis.PGgeometry#splitSRID]] */
  private def splitRSIDAndWKT(value: String): (Int, String) = {
    if (value.startsWith("SRID=")) {
      val index = value.indexOf(';', 5) // srid prefix length is 5
      if (index == -1) {
        throw new java.sql.SQLException("Error parsing Geometry - SRID not delimited with ';' ")
      } else {
        val srid = Integer.parseInt(value.substring(0, index))
        val wkt = value.substring(index + 1)
        (srid, wkt)
      }
    } else (-1, value)
  }
}
