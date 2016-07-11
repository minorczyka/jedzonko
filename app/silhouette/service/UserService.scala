package silhouette.service

import com.mohiva.play.silhouette.api.services.IdentityService
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import silhouette.SilhouetteUser

import scala.concurrent.Future

trait UserService extends IdentityService[SilhouetteUser] {

  def save(user: SilhouetteUser): Future[SilhouetteUser]

  def save(profile: CommonSocialProfile): Future[SilhouetteUser]
}
