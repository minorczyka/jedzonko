package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

object CreateGroupForm {

  val form = Form(
    mapping(
      "groupName" -> nonEmptyText,
      "groupPassword1" -> nonEmptyText,
      "groupPassword2" -> nonEmptyText
    )(Data.apply)(Data.unapply)
      verifying(Messages("auth.passwords.notequal"), data => data.groupPassword1 == data.groupPassword2)
  )

  case class Data(groupName: String,
                  groupPassword1: String,
                  groupPassword2: String)
}
