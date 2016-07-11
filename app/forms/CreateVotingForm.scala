package forms

import java.util.Date

import play.api.data.Form
import play.api.data.Forms._
import play.api.data.format.Formats._
import play.api.Play.current
import play.api.i18n.Messages
import play.api.i18n.Messages.Implicits._

object CreateVotingForm {

  val form = Form(
    mapping(
      "groupId" -> number,
      "placesIds" -> seq(number),
      "votingEnd" -> optional(date("yyyy-MM-dd HH:mm:ss")),
      "orderEnd" -> optional(date("yyyy-MM-dd HH:mm:ss"))
    )(Data.apply)(Data.unapply)
  )

  case class Data(groupId: Int,
                  placesIds: Seq[Int],
                  votingEnd: Option[Date],
                  orderEnd: Option[Date])
}
