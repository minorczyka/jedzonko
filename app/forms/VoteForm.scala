package forms

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

object VoteForm {

  val form = Form(
    mapping(
      "placesIds" -> seq(number)
    )(Data.apply)(Data.unapply)
  )

  case class Data(placesIds: Seq[Int])
}
