package silhouette.service

import java.util.UUID

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import dao.UserDao
import silhouette.SilhouetteUser
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future

class UserServiceImpl @Inject() (userDao: UserDao) extends UserService {

  def retrieve(loginInfo: LoginInfo): Future[Option[SilhouetteUser]] = userDao.find(loginInfo)

  def save(user: SilhouetteUser) = userDao.save(user)

  def save(profile: CommonSocialProfile) = {
    userDao.find(profile.loginInfo).flatMap {
      case Some(user) =>
        userDao.save(user.copy(
          firstName = profile.firstName,
          lastName = profile.lastName,
          fullName = profile.fullName,
          email = profile.email,
          avatarURL = profile.avatarURL
        ))
      case None => // Insert a new user
        userDao.save(SilhouetteUser(
          userId = 0,
          loginInfo = profile.loginInfo,
          firstName = profile.firstName,
          lastName = profile.lastName,
          fullName = profile.fullName,
          email = profile.email,
          avatarURL = profile.avatarURL,
          privileges = SilhouetteUser.NormalUser
        ))
    }
  }
}
