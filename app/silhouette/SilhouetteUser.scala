package silhouette

import java.util.UUID

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}

case class SilhouetteUser(
  userId: Int,
  loginInfo: LoginInfo,
  firstName: Option[String],
  lastName: Option[String],
  fullName: Option[String],
  email: Option[String],
  avatarURL: Option[String],
  privileges: Short) extends Identity

object SilhouetteUser {
  val NormalUser : Short = 0;
  val Admin : Short = 1;
}