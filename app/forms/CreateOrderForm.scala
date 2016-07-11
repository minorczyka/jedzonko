package forms

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

object CreateOrderForm {

  val form = Form(
    mapping(
      "groupId" -> number,
      "placesIds" -> number,
      "orderEnd" -> optional(date("yyyy-MM-dd HH:mm:ss")),
      "discount" -> optional(of(floatFormat)),
      "additionalCost" -> optional(of(floatFormat))
    )(Data.apply)(Data.unapply)
  )

  case class Data(groupId: Int,
                  placesIds: Int,
                  orderEnd: Option[Date],
                  discount: Option[Float],
                  additionalCost: Option[Float])
}
