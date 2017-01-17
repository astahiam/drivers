package responses

case class DriverResponse
(
  id:Long,
  name:String,
  vehicle:String
)

case class SeqDriverResponse
(
  drivers: Seq[DriverResponse]
)