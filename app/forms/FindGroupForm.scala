package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

object FindGroupForm {

  val form = Form(
    mapping(
      "groupName" -> nonEmptyText
    )(Data.apply)(Data.unapply)
  )

  case class Data(groupName: String)
}
