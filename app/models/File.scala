package models

import java.util.Date
import play.api.libs.json.Json

/**
  * Created by jaideep on 01/06/17.
  */
case class File(name: String, size: Int, uploaded: Date)

object File {
  implicit val formatter = Json.format[File]
}