package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

object JoinGroupForm {

  val form = Form(
    mapping(
      "groupPassword" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(groupPassword: String)
}
