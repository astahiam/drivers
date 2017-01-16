package responses

import play.api.libs.json.{Json, JsValue}

object ResponseWrapper {
  def apply(meta: JsValue, data: JsValue): JsValue = Json.obj(
    "meta" -> meta,
    "data" -> data
  )
}