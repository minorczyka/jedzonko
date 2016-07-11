package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import dao._
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import silhouette.{PrivilegesAuthorization, SilhouetteUser}

class ReportController @Inject()(
  val messagesApi: MessagesApi,
  val env: Environment[SilhouetteUser, CookieAuthenticator],
  val socialProviderRegistry: SocialProviderRegistry,
  val userDao: UserDao,
  val groupDao: GroupDao,
  val placeDao: PlaceDao,
  val votingDao: VotingDao,
  val orderDao: OrderDao) extends Silhouette[SilhouetteUser, CookieAuthenticator] {

  def reports = SecuredAction(PrivilegesAuthorization(UserDao.admin)) { implicit request =>
    Ok(views.html.admin.reports(request.identity))
  }

  def allUsers = SecuredAction(PrivilegesAuthorization(UserDao.admin)).async { implicit request =>
    userDao.allUsers.map(u =>
      Ok(views.xml.admin.xml.users(u))
    )
  }

  def allGroups = SecuredAction(PrivilegesAuthorization(UserDao.admin)).async { implicit request =>
    groupDao.allGroups.map(g =>
      Ok(views.xml.admin.xml.groups(g))
    )
  }

  def allPlaces = SecuredAction(PrivilegesAuthorization(UserDao.admin)).async { implicit request =>
    placeDao.allPlaces.map(p =>
      Ok(views.xml.admin.xml.places(p))
    )
  }

  def allVotings = SecuredAction(PrivilegesAuthorization(UserDao.admin)).async { implicit request =>
    votingDao.allVotings.map(v =>
      Ok(views.xml.admin.xml.votings(v))
    )
  }

  def allOrders = SecuredAction(PrivilegesAuthorization(UserDao.admin)).async { implicit request =>
    orderDao.allOrders.map(o =>
      Ok(views.xml.admin.xml.orders(o))
    )
  }
}
