package controllers

import java.sql.Timestamp

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import dao.{GroupDao, OrderDao, PlaceDao}
import forms.{ChatMessageForm, CreateOrderForm, CreateUserOrderForm, EditUserOrderForm}
import models.{OrderMessageRow, OrderRow, UserToOrderRow}
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.AnyContent
import silhouette.SilhouetteUser

import scala.concurrent.Future

class OrderController @Inject()(
  val messagesApi: MessagesApi,
  val env: Environment[SilhouetteUser, CookieAuthenticator],
  val groupDao: GroupDao,
  val placeDao: PlaceDao,
  val orderDao: OrderDao) extends Silhouette[SilhouetteUser, CookieAuthenticator] {

  private def canUserViewOrder(user: SilhouetteUser, orderId: Int) : Future[Option[OrderRow]] = {
    orderDao.findUserOrders(user.userId).map { orders =>
      orders.filter(o => o._2.id == orderId).headOption.map(o => o._2)
    }
  }

  private def doUserOwnsOrder(user: SilhouetteUser, orderId: Int) : Future[Option[OrderRow]] = {
    orderDao.find(orderId).map {
      case Some(order) => if (order.authorId == user.userId) Some(order) else None
      case None => None
    }
  }

  def createOrder = SecuredAction.async { implicit request =>
    groupDao.findUserGroups(request.identity).map { groups =>
      val select = groups.map(g => (g.id.toString, g.name))
      Ok(views.html.order.createOrder(request.identity, select, CreateOrderForm.form))
    }
  }

  def deleteOrder(orderId: Int) = SecuredAction.async { implicit request =>
    doUserOwnsOrder(request.identity, orderId).flatMap {
      case Some(order) => {
        orderDao.deleteOrder(orderId).map(_ => Redirect(routes.OrderController.listOrders))
      }
      case None => Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
    }
  }

  def closeOrder(orderId: Int) = SecuredAction.async { implicit request =>
    doUserOwnsOrder(request.identity, orderId).flatMap {
      case Some(order) => {
        if (order.status == OrderDao.orderStarted) {
          orderDao.closeOrder(orderId).map(_ => Redirect(routes.OrderController.getOrderDetails(orderId)))
        } else {
          Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
        }
      }
      case None => Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
    }
  }

  def openOrder(orderId: Int) = SecuredAction.async { implicit request =>
    doUserOwnsOrder(request.identity, orderId).flatMap {
      case Some(order) => {
        if (order.status == OrderDao.orderClosed) {
          orderDao.openOrder(orderId).map(_ => Redirect(routes.OrderController.getOrderDetails(orderId)))
        } else {
          Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
        }
      }
      case None => Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
    }
  }

  def handleCreateOrder = SecuredAction.async { implicit request =>
    CreateOrderForm.form.bindFromRequest.fold(
      form => {
        groupDao.findUserGroups(request.identity).map { groups =>
          val select = groups.map(g => (g.id.toString, g.name))
          Ok(views.html.order.createOrder(request.identity, select, form))
        }
      },
      data => {
        groupDao.findUserGroups(request.identity).flatMap { groups =>
          if (groups.exists(g => g.id == data.groupId)) {
            orderDao.insertOrder(OrderRow(
              0,
              data.groupId,
              request.identity.userId,
              data.placesIds,
              OrderDao.orderStarted,
              new Timestamp(System.currentTimeMillis),
              data.orderEnd.map(d => new Timestamp(d.getTime)),
              data.discount,
              data.additionalCost
            )).map(_ => Redirect(routes.OrderController.listOrders))
          } else {
            Future.successful(NotFound(views.html.group.groupNotFound(request.identity)))
          }
        }
      }
    )
  }

  def listOrders = SecuredAction.async { implicit request =>
    orderDao.findUserOrdersWithDetails(request.identity.userId).map(orders =>
      Ok(views.html.order.listOrders(request.identity, orders))
    )
  }

  private def orderDetails(orderId: Int, form: Option[Form[CreateUserOrderForm.Data]] = None)(implicit request: OrderController.this.SecuredRequest[AnyContent]) = {
    canUserViewOrder(request.identity, orderId).flatMap {
      case Some(order) => {
        placeDao.find(order.placeId).flatMap {
          case Some(place) => {
            groupDao.findGroupMembers(order.groupId).flatMap { m =>
              val members = m.map(u => u.id -> u).toMap
              orderDao.getUserToOrderWithUsers(orderId).flatMap { userToOrder =>
                orderDao.findOrderMessages(orderId).map { messages =>
                  form match {
                    case Some(f) => Ok(views.html.order.orderDetails(request.identity, order, place, members, userToOrder, messages, f))
                    case None => Ok(views.html.order.orderDetails(request.identity, order, place, members, userToOrder, messages, CreateUserOrderForm.form))
                  }
                }
              }
            }
          }
          case None => Future.successful(NotFound(views.html.place.placeNotFound(request.identity)))
        }
      }
      case None => Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
    }
  }

  def getOrderDetails(orderId: Int) = SecuredAction.async { implicit request =>
    orderDetails(orderId)
  }

  def handleAddMessage(orderId: Int) = SecuredAction.async { implicit request =>
    doUserOwnsOrder(request.identity, orderId).flatMap {
      case Some(order) => {
        ChatMessageForm.form.bindFromRequest.fold(
          form => Future.successful(Redirect(routes.OrderController.getOrderDetails(orderId))),
          data => {
            orderDao.insertComment(OrderMessageRow(
              0,
              orderId,
              data.message,
              new Timestamp(System.currentTimeMillis)
            )).map(_ => Redirect(routes.OrderController.getOrderDetails(orderId)))
          }
        )
      }
      case None => Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
    }
  }

  def handleAddUserOrder(orderId: Int) = SecuredAction.async { implicit request =>
    canUserViewOrder(request.identity, orderId).flatMap {
      case Some(order) => {
        if (order.status == OrderDao.orderStarted) {
          CreateUserOrderForm.form.bindFromRequest.fold(
            form => orderDetails(orderId, Some(form)),
            data => {
              orderDao.insertUserToOrder(UserToOrderRow(
                0,
                orderId,
                request.identity.userId,
                data.subject,
                data.additionalInfo,
                data.cost,
                OrderDao.orderNotPayed
              )).map(_ => Redirect(routes.OrderController.getOrderDetails(orderId)))
            }
          )
        } else {
          Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
        }
      }
      case None => Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
    }
  }

  def editUserOrder = SecuredAction.async { implicit request =>
    EditUserOrderForm.form.bindFromRequest.fold(
      form => Future.successful(Redirect(routes.OrderController.listOrders)),
      data => {
        orderDao.findUserOrder(data.userOrderId).flatMap {
          case Some(userOrder) => {
            if (userOrder.userId == request.identity.userId) {
              orderDao.find(userOrder.orderId).flatMap {
                case Some(order) => {
                  if (order.status == OrderDao.orderStarted) {
                    orderDao.updateUserOrder(userOrder.copy(
                      subject = data.subject,
                      additionalInfo = data.additionalInfo,
                      cost = data.cost
                    )).map(x => Some(x))
                  } else {
                    Future.successful(None)
                  }
                }
                case None => Future.successful(None)
              }
            } else {
              Future.successful(None)
            }
          }
          case None => Future.successful(None)
        }.map {
          case Some(userOrder) => Redirect(routes.OrderController.getOrderDetails(userOrder.orderId))
          case None => BadRequest(views.html.order.orderNotFound(request.identity))
        }
      }
    )
  }

  def deleteUserOrder(userOrderId: Int) = SecuredAction.async { implicit request =>
    orderDao.findUserOrder(userOrderId).flatMap {
      case Some(userOrder) => {
        orderDao.find(userOrder.orderId).flatMap {
          case Some(order) => {
            if (userOrder.userId == request.identity.userId && order.status == OrderDao.orderStarted) {
              orderDao.deleteUserOrder(userOrderId).map(_ => Redirect(routes.OrderController.getOrderDetails(userOrder.orderId)))
            } else {
              Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
            }
          }
          case None => Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
        }
      }
      case None => Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
    }
  }

  def finalizeOrder(orderId: Int) = SecuredAction.async { implicit request =>
    doUserOwnsOrder(request.identity, orderId).flatMap {
      case Some(order) => {
        groupDao.findGroupMembers(order.groupId).flatMap { m =>
          val members = m.map(u => u.id -> u).toMap
          orderDao.getUserToOrderWithUsers(orderId).map { userToOrder =>
            Ok(views.html.order.finalizeOrder(request.identity, order, members, userToOrder))
          }
        }
      }
      case None => Future.successful(NotFound(views.html.order.orderNotFound(request.identity)))
    }
  }

  def findOrder(userToOrderId: Int) = SecuredAction.async { implicit request =>
    orderDao.findUserOrder(userToOrderId).map {
      case Some(userToOrder) => Redirect(routes.OrderController.getOrderDetails(userToOrder.orderId))
      case None => NotFound(views.html.order.orderNotFound(request.identity))
    }
  }
}
