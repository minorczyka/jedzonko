package dao

import com.google.inject.{Inject, Singleton}
import models._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

@Singleton
class OrderDao @Inject()(val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import Tables.profile.api._

  def insertOrder(order: OrderRow) : Future[OrderRow] = {
    db.run((Tables.Order returning Tables.Order) += order)
  }

  def deleteOrder(orderId: Int) : Future[Unit] = {
    val q = Tables.Order.filter(_.id === orderId)
    db.run(q.delete).map(_ => ())
  }

  def closeOrder(orderId: Int) : Future[Unit] = {
    val q = for {
      o <- Tables.Order.filter(_.id === orderId)
    } yield(o.status)
    db.run(q.update(OrderDao.orderClosed)).map(_ => ())
  }

  def openOrder(orderId: Int) : Future[Unit] = {
    val q = for {
      o <- Tables.Order.filter(_.id === orderId)
    } yield(o.status)
    db.run(q.update(OrderDao.orderStarted)).map(_ => ())
  }

  def updateOrder(order: OrderRow) : Future[OrderRow] = {
    val q = Tables.Order.filter(_.id === order.id)
    db.run(q.update(order)).map(_ => order)
  }

  def findUserOrders(userId: Int) : Future[Seq[(GroupRow, OrderRow)]] = {
    val q = for {
      (((u, ug), g), o) <- Tables.User join Tables.UserToGroup on (_.id === _.userId) join Tables.Group on (_._2.groupId === _.id) join Tables.Order on (_._2.id === _.groupId)
      if (u.id === userId)
    } yield(g, o)
    db.run(q.result).map(x => x)
  }

  def findUserOrdersWithDetails(userId: Int) : Future[Seq[(GroupRow, OrderRow, UserRow, PlaceRow)]] = {
    val q = for {
      (((((u, ug), g), o), a), p) <- Tables.User join Tables.UserToGroup on (_.id === _.userId) join Tables.Group on (_._2.groupId === _.id) join Tables.Order on (_._2.id === _.groupId) join Tables.User on (_._2.authorId === _.id) join Tables.Place on (_._1._2.placeId === _.id)
      if (u.id === userId)
    } yield(g, o, a, p)
    db.run(q.result).map(x => x)
  }

  def find(orderId: Int) : Future[Option[OrderRow]] = {
    val q = Tables.Order.filter(_.id === orderId)
    db.run(q.result.headOption)
  }

  def getUserToOrder(orderId: Int) : Future[Seq[UserToOrderRow]] = {
    val q = for {
      (uo, o) <- Tables.UserToOrder join Tables.Order on (_.orderId === _.id)
      if (o.id === orderId)
    } yield(uo)
    db.run(q.result).map(x => x)
  }

  def getUserToOrderWithUsers(orderId: Int) : Future[Seq[(UserRow, UserToOrderRow)]] = {
    val q = for {
      ((u, uo), o) <- Tables.User join Tables.UserToOrder on (_.id === _.userId) join Tables.Order on (_._2.orderId === _.id)
      if (o.id === orderId)
    } yield(u, uo)
    db.run(q.result).map(x => x)
  }

  def insertComment(comment: OrderMessageRow) : Future[OrderMessageRow] = {
    db.run((Tables.OrderMessage returning Tables.OrderMessage) += comment).map(c => c)
  }

  def findOrderMessages(orderId: Int) : Future[Seq[OrderMessageRow]] = {
    val q = Tables.OrderMessage.filter(_.orderId === orderId)
    db.run(q.result)
  }

  def insertUserToOrder(userToOrder: UserToOrderRow) : Future[UserToOrderRow] = {
    db.run((Tables.UserToOrder returning Tables.UserToOrder) += userToOrder)
  }

  def findUserOrder(userOrderId: Int) : Future[Option[UserToOrderRow]] = {
    val q = Tables.UserToOrder.filter(_.id === userOrderId)
    db.run(q.result.headOption)
  }

  def deleteUserOrder(userOrderId: Int) : Future[Unit] = {
    val q = Tables.UserToOrder.filter(_.id === userOrderId)
    db.run(q.delete).map(_ => ())
  }

  def updateUserOrdersPrices(orderId: Int, userOrders: Seq[(Int, Option[Float])]) : Future[Unit] = {
    val actions = (for {
      _ <- DBIO.seq(userOrders.map { uo =>
        val q = for {
          x <- Tables.UserToOrder
          if (x.id === uo._1)
        } yield(x.cost)
        q.update(uo._2)
      }: _*)
    } yield()).transactionally
    db.run(actions)
  }

  def updateUserToOrder(orderId: Int, userId: Int, status: Short) : Future[Unit] = {
    val q = Tables.UserToOrder.filter(uo => uo.orderId === orderId && uo.userId === userId).map(uo => uo.status)
    db.run(q.update(status)).map(_ => ())
  }

  def allOrders : Future[Seq[OrderRow]] = {
    db.run(Tables.Order.result)
  }
}

object OrderDao {
  val orderStarted : Short = 0
  val orderClosed : Short = 1
  val orderFinished : Short = 2

  val orderNotPayed : Short = 0
  val orderPayed : Short = 1
}
