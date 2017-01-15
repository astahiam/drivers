package requests

case class SearchDriverRequest
(
  latitude: Double,
  longitude: Double,
  radius: Long,
  limit: Int
)