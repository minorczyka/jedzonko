package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

object EditUserOrderForm {

  val form = Form(
    mapping(
      "userOrderId" -> number,
      "editSubject" -> nonEmptyText,
      "editAdditionalInfo" -> optional(text),
      "editCost" -> optional(of(floatFormat))
    )(Data.apply)(Data.unapply)
  )

  case class Data(userOrderId: Int,
                  subject: String,
                  additionalInfo: Option[String],
                  cost: Option[Float])
}


