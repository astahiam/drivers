package requests

case class UpdateDriverRequest
(
  driverId: Long,
  latitude: Double,
  longitude: Double
)