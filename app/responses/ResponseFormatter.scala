package responses

import java.sql.Timestamp
import java.text.SimpleDateFormat
import play.api.libs.json.{Json, JsString, JsValue, Writes}
import models.{DriverModel, DriverLocationModel}

trait ResponseFormatter {

  implicit val timestampWriter = new Writes[Timestamp] {
    override def writes(t: Timestamp): JsValue = JsString(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(t))
  }

  implicit val driverResponse = Json.writes[DriverResponse]
  implicit val seqDriverResponse = Json.writes[SeqDriverResponse]

  def toDriverResponse(driver: DriverModel): DriverResponse = {
    DriverResponse(
        id = driver.id,
        name = driver.name,
        vehicle = driver.vehicle
    )
  }

  def toDriverListResponse(seqDrv: Seq[DriverModel]): SeqDriverResponse = {
    SeqDriverResponse(
      drivers = seqDrv.map(toDriverResponse)
    )
  }

}

object ResponseFormatter extends ResponseFormatter