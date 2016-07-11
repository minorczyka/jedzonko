package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

object ChatMessageForm {

  val form = Form(
    mapping(
      "message" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(message: String)
}
