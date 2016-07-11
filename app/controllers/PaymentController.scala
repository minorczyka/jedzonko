package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import dao.PaymentDao
import play.Logger
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import silhouette.SilhouetteUser

import scala.concurrent.Future

class PaymentController @Inject()(
  val messagesApi: MessagesApi,
  val env: Environment[SilhouetteUser, CookieAuthenticator],
  val paymentDao: PaymentDao) extends Silhouette[SilhouetteUser, CookieAuthenticator] {

  def incomes = SecuredAction.async { implicit request =>
    paymentDao.getUserIncomes(request.identity.userId).map(incomes =>
      Ok(views.html.payment.incomes(request.identity, incomes))
    )
  }

  def charges = SecuredAction.async { implicit request =>
    paymentDao.getUserCharges(request.identity.userId).map(charges =>
      Ok(views.html.payment.charges(request.identity, charges))
    )
  }

  def prepaid = SecuredAction.async { implicit request =>
    paymentDao.userPrepaids(request.identity.userId).map { p =>
      val prepaids = p.partition(x => x._1.payerId == request.identity.userId)
      val positive = prepaids._1.groupBy(x => x._1.receiverId).map(x => (x._1, x._2.map(y => y._1.amount).sum))
      val negative = prepaids._2.groupBy(x => x._1.payerId).map(x => (x._1, x._2.map(y => y._1.amount).sum))
      val users = (p.map(x => (x._1.payerId, x._2)) ++ p.map(x => (x._1.receiverId, x._3))).distinct.filter(x => x._1 != request.identity.userId)
      val balances = users.map(x => (x._1, x._2, positive.getOrElse(x._1, 0.0f) - negative.getOrElse(x._1, 0.0f))).filter(x => x._3 != 0.0f)
      Ok(views.html.payment.prepaid(request.identity, p, balances))
    }
  }
}