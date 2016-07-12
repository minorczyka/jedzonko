package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, LogoutEvent, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import dao.{GroupDao, OrderDao, UserDao, VotingDao}
import forms.{SignInForm, SignUpForm}
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.{Cookie, DiscardingCookie}
import silhouette.SilhouetteUser

class ApplicationController @Inject()(
  val messagesApi: MessagesApi,
  val env: Environment[SilhouetteUser, CookieAuthenticator],
  val socialProviderRegistry: SocialProviderRegistry,
  val userDao: UserDao,
  val groupDao: GroupDao,
  val votingDao: VotingDao,
  val orderDao: OrderDao) extends Silhouette[SilhouetteUser, CookieAuthenticator] {

  def index = SecuredAction.async { implicit request =>
    val incomes = userDao.countUserIncomes(request.identity.userId)
    val charges = userDao.countUserCharges(request.identity.userId)
    val votings = userDao.countUserAvailableVotings(request.identity.userId)
    val orders = userDao.countUserAvailableOrders(request.identity.userId)
    val groups = groupDao.findUserGroups(request.identity)
    (for {
      i <- incomes
      c <- charges
      v <- votings
      o <- orders
      g <- groups.map(x => x.headOption)
    } yield(i, c, v, o, g)).map { x =>
      val stats = (x._1, x._2, x._3, x._4, x._5)
      Ok(views.html.home(request.identity, stats))
    }
  }

  def latestVoting = SecuredAction.async { implicit request =>
    votingDao.findLatestOpenUserVoting(request.identity.userId).map {
      case Some(votingRow) => Redirect(routes.VotingController.votingDetails(votingRow.id))
      case None => Redirect(routes.VotingController.listVotings)
    }
  }

  def latestOrder = SecuredAction.async { implicit request =>
    orderDao.findLatestOpenUserOrder(request.identity.userId).map {
      case Some(orderRow) => Redirect(routes.OrderController.getOrderDetails(orderRow.id))
      case None => Redirect(routes.OrderController.listOrders)
    }
  }

  def signIn = UserAwareAction { implicit request =>
    request.identity match {
      case Some(user) => Redirect(routes.ApplicationController.index)
      case None => Ok(views.html.signIn(SignInForm.form, socialProviderRegistry)).discardingCookies(DiscardingCookie("redirectionUrl"))
    }
  }

  def signInAndRedirect(redirect: String) = UserAwareAction { implicit request =>
    request.identity match {
      case Some(user) => Redirect(routes.ApplicationController.index)
      case None => Ok(views.html.signIn(SignInForm.form, socialProviderRegistry)).withCookies(Cookie("redirectionUrl", redirect))
    }
  }

  def signUp = UserAwareAction { implicit request =>
    request.identity match {
      case Some(user) => Redirect(routes.ApplicationController.index)
      case None => Ok(views.html.signUp(SignUpForm.form))
    }
  }

  def signOut = SecuredAction.async { implicit request =>
    val result = Redirect(routes.ApplicationController.signIn)
    env.eventBus.publish(LogoutEvent(request.identity, request, request2Messages))

    env.authenticatorService.discard(request.authenticator, result)
  }
}