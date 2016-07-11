package silhouette

import com.mohiva.play.silhouette.api.Authorization
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import play.api.i18n.Messages
import play.api.mvc.Request

import scala.concurrent.Future

case class PrivilegesAuthorization(privileges: Short) extends Authorization[SilhouetteUser, CookieAuthenticator] {

  def isAuthorized[B](user: SilhouetteUser, authenticator: CookieAuthenticator)(implicit request: Request[B], messages: Messages) = {
    Future.successful(user.privileges >= privileges)
  }
}
