package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

/**
  * The form which handles the sign up process.
  */
object SignUpForm {

  /**
    * A play framework form.
    */
  val form = Form(
    mapping(
      "firstName" -> nonEmptyText,
      "lastName" -> nonEmptyText,
      "email" -> email,
      "password1" -> nonEmptyText,
      "password2" -> nonEmptyText
    )(Data.apply)(Data.unapply)
      verifying(Messages("auth.passwords.notequal"), data => data.password1 == data.password2)
  )

  case class Data(firstName: String,
                  lastName: String,
                  email: String,
                  password1: String,
                  password2: String)
}