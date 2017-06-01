package models

import play.api.libs.json.Json
import java.util.Date

case class Person(name: String, age: Int, created: Date)

object Person {
  implicit val formatter = Json.format[Person]
}
