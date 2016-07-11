package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

object CreatePlaceForm {

  val form = Form(
    mapping(
      "groupId" -> number,
      "name" -> nonEmptyText,
      "url" -> optional(nonEmptyText),
      "minimumOrder" -> optional(of(floatFormat)),
      "deliveryCost" -> optional(of(floatFormat))
    )(Data.apply)(Data.unapply)
  )

  case class Data(groupId: Int,
                  name: String,
                  url: Option[String],
                  minimumOrder: Option[Float],
                  deliveryCost: Option[Float])
}
