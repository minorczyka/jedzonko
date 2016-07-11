package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.Play.current
import play.api.data.format.Formats._
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

object PrepaidForm {

  val form = Form(
    mapping(
      "amount" -> of(floatFormat).verifying(x => x > 0.0)
    )(Data.apply)(Data.unapply)
  )

  case class Data(amount: Float)
}
