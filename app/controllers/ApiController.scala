package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import dao._
import models.{OrderRow, PlaceRow}
import play.Logger
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.{AnyContent, RequestHeader}
import silhouette.SilhouetteUser

import scala.concurrent.Future

class ApiController @Inject()(
  val groupDao: GroupDao,
  val placeDao: PlaceDao,
  val orderDao: OrderDao,
  val paymentDao: PaymentDao,
  val messagesApi: MessagesApi,
  val env: Environment[SilhouetteUser, CookieAuthenticator]) extends Silhouette[SilhouetteUser, CookieAuthenticator] {

  override def onNotAuthenticated(request: RequestHeader) = {
    Some(Future.successful(Unauthorized(Json.obj("error" -> Messages("auth.error")))))
  }

  implicit val placeFormat = Json.format[PlaceRow]

  def getGroupPlaces(groupId: Int) = SecuredAction.async { implicit  request =>
    groupDao.find(groupId).flatMap {
      case Some(group) => placeDao.findGroupPlaces(groupId).map(places => Ok(Json.toJson(places)))
      case None => Future.successful(BadRequest(Json.obj("error" -> Messages("group.not.found"))))
    }
  }

  implicit val orderRowFormat = Json.format[Order]
  implicit val orderInfoFormat = Json.format[OrderInfo]

  def saveOrder = SecuredAction.async { implicit request =>
    updateOrder(None).map {
      case Some(order) => Ok(Json.obj("message" -> "ok"))
      case None => BadRequest(Json.obj("error" -> Messages("order.not.found")))
    }
  }

  def submitOrder = SecuredAction.async { implicit request =>
    updateOrder(Some(OrderDao.orderFinished)).flatMap {
      case Some(order) => {
        orderDao.updateUserToOrder(order.id, request.identity.userId, OrderDao.orderPayed).map(_ => Ok(Json.obj("message" -> "ok")))
      }
      case None => Future.successful(BadRequest(Json.obj("error" -> Messages("order.not.found"))))
    }
  }

  private def updateOrder(orderStatus: Option[Short])(implicit request: ApiController.this.SecuredRequest[AnyContent]) : Future[Option[OrderRow]] = {
    request.body.asJson.map { json =>
      json.validate[OrderInfo].map { o =>
        orderDao.find(o.orderId).flatMap {
          case Some(order) if (order.authorId == request.identity.userId) => {
            val orders = orderStatus match {
              case Some(OrderDao.orderFinished) => considerAdditionalCosts(o)
              case _ => Future.successful(o.orders.map(x => (x.id, x.cost)))
            }
            val newOrder = order.copy(discount = o.discount,
              additionalCost = o.additionalCost,
              status = orderStatus.getOrElse(order.status))
            orderDao.updateOrder(newOrder).flatMap(_ =>
              orders.flatMap(os =>
                orderDao.updateUserOrdersPrices(order.id, os).map(_ =>
                  Some(order)
                )
              )
            )
          }
        }
      }.recoverTotal{
        e => Future.successful(None)
      }
    }.getOrElse(Future.successful(None))
  }

  private def considerAdditionalCosts(orderInfo: OrderInfo) : Future[Seq[(Int, Option[Float])]] = {
    orderDao.getUserToOrder(orderInfo.orderId).map { orders =>
      val userOrders = orders.groupBy(x => x.userId)
      val orderMap = orderInfo.orders.map(x => (x.id, x.cost)).toMap
      val additionalCost = orderInfo.additionalCost.getOrElse(0.0f) / userOrders.size
      orders.map { order =>
        val cost = orderMap.getOrElse(order.id, order.cost).getOrElse(0.0f)
        (order.id, Some(cost * (1.0f - orderInfo.discount.getOrElse(0.0f) / 100.0f) + (additionalCost / userOrders(order.userId).size)))
      }
    }
  }

  case class Order(val id: Int, val cost: Option[Float])
  case class OrderInfo(val orderId: Int, val discount: Option[Float], val additionalCost: Option[Float], val orders: Seq[Order])

  def acceptIncome(incomeId: Int) = SecuredAction.async { implicit request =>
    paymentDao.findUserToOrder(incomeId).map {
      case Some((userToOrder, order)) => {
        if (order.authorId == request.identity.userId) Some(userToOrder) else None
      }
      case None => None
    }.flatMap {
      case Some(userToOrder) => {
        paymentDao.acceptIncome(incomeId).map(_ => Ok(Json.obj("message" -> "ok")))
      }
      case None => Future.successful(BadRequest(Json.obj("error" -> "error")))
    }
  }

  def payWithPrepaid(chargeId: Int) = SecuredAction.async { implicit request =>
    paymentDao.findUserToOrder(chargeId).map {
      case Some((userToOrder, order)) => {
        if (userToOrder.status == OrderDao.orderNotPayed) Some(userToOrder, order) else None
      }
      case None => {
        None
      }
    }.flatMap {
      case Some((userToOrder, order)) => {
        paymentDao.payWithPrepaid(request.identity.userId, order.authorId, userToOrder.id).map(x =>
          Ok(Json.obj("message" -> (if (x) "ok" else "error"))))
      }
      case None => Future.successful(BadRequest(Json.obj("error" -> "error")))
    }
  }
}
