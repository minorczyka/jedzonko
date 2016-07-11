package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import dao.{PaymentDao, UserDao}
import forms.PrepaidForm
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import silhouette.SilhouetteUser

import scala.concurrent.Future

class UserController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[SilhouetteUser, CookieAuthenticator],
  val userDao: UserDao,
  val paymentDao: PaymentDao) extends Silhouette[SilhouetteUser, CookieAuthenticator] {

  private def getUserDetails(id: Int, form: Form[PrepaidForm.Data]) = SecuredAction.async { implicit request =>
    userDao.findUser(id).flatMap {
      case Some(user) => {
        val groups = userDao.countUserGroups(id)
        val places = userDao.countUserPlaces(id)
        val votings = userDao.countUserVotings(id)
        val orders = userDao.countUserOrders(id)
        val balance = if (id != request.identity.userId) {
          paymentDao.prepaidBalance(request.identity.userId, id).map(x => Some(x))
        } else {
          Future.successful(None)
        }
        (for {
          g <- groups
          p <- places
          v <- votings
          o <- orders
          b <- balance
        } yield(g, p, v, o, b)).map { x =>
          val stats = (x._1, x._2, x._3, x._4, x._5)
          Ok(views.html.user.userDetails(request.identity, user, stats, form))
        }
      }
      case None => Future.successful(Ok(views.html.user.userNotFound(request.identity)))
    }
  }

  def userDetails(id: Int) = SecuredAction.async { implicit request =>
    getUserDetails(id, PrepaidForm.form).apply(request)
  }

  def handlePrepaid(id: Int) = SecuredAction.async { implicit request =>
    PrepaidForm.form.bindFromRequest.fold(
      form => getUserDetails(id, form).apply(request),
      data => {
        paymentDao.acceptPrepaidMoney(id, request.identity.userId, data.amount).map(_ =>
          Redirect(routes.UserController.userDetails(id))
        )
      }
    )
  }
}