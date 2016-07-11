package dao

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import models._
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

@Singleton
class PaymentDao @Inject()(val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import Tables.profile.api._

  def getUserIncomes(userId: Int) : Future[Seq[(UserRow, UserToOrderRow, OrderRow)]] = {
    val q = for {
      ((o, uo), u) <- Tables.Order join Tables.UserToOrder on (_.id === _.orderId) join Tables.User on (_._2.userId === _.id)
      if (o.authorId === userId && o.status === OrderDao.orderFinished && !(uo.userId === userId))
    } yield(u, uo, o)
    db.run(q.result).map(x => x)
  }

  def getUserCharges(userId: Int) : Future[Seq[(UserRow, UserToOrderRow, OrderRow)]] = {
    val q = for {
      ((uo, o), u) <- Tables.UserToOrder join Tables.Order on (_.orderId === _.id) join Tables.User on (_._2.authorId === _.id)
      if (uo.userId === userId && o.status === OrderDao.orderFinished && !(o.authorId === userId))
    } yield(u, uo, o)
    db.run(q.result).map(x => x)
  }

  def findUserToOrder(id: Int) : Future[Option[(UserToOrderRow, OrderRow)]] = {
    val q = for {
      (uo, o) <- Tables.UserToOrder join Tables.Order on (_.orderId === _.id)
      if (uo.id === id)
    } yield(uo, o)
    db.run(q.result.headOption).map(x => x)
  }

  def acceptIncome(id: Int) : Future[Unit] = {
    val q = Tables.UserToOrder.filter(_.id === id).map(uo => uo.status)
    db.run(q.update(OrderDao.orderPayed)).map(_ => ())
  }

  def acceptPrepaidMoney(payer: Int, receiver: Int, amount: Float) : Future[PrepaidRow] = {
    val now = new Timestamp(System.currentTimeMillis())
    val action = (Tables.Prepaid returning Tables.Prepaid) += PrepaidRow(0, receiver, payer, None, amount, now, PaymentDao.prepaidStorage)
    db.run(action).map(p => p)
  }

  def prepaidBalance(payer: Int, receiver: Int) : Future[Float] = {
    val q = Tables.Prepaid.filter(p => (p.payerId === payer && p.receiverId === receiver) ||
      (p.payerId === receiver && p.receiverId === payer)).map(p => (p.payerId, p.receiverId, p.amount))
    db.run(q.result).map { list =>
      val prepaids = list.partition(p => p._1 == payer)
      val positive = prepaids._1.map(p => p._3).sum
      val negative = prepaids._2.map(p => p._3).sum
      positive - negative
    }
  }

  def payWithPrepaid(payer: Int, receiver: Int, userToOrderId: Int) : Future[Boolean] = {
    val actions = (for {
      balance <- DBIO.from(prepaidBalance(payer, receiver))
      uo <- Tables.UserToOrder.filter(_.id === userToOrderId).result.head
      enough <- DBIO.from(Future.successful(uo.cost.getOrElse(0.0f) <= balance))
      _ <- DBIO.from(Future.successful {
        if (enough) db.run(Tables.UserToOrder.filter(_.id === uo.id).map(x => x.status).update(OrderDao.orderPayed))
      })
      _ <- DBIO.from(Future.successful {
        if (enough) {
          val now = new Timestamp(System.currentTimeMillis())
          db.run(Tables.Prepaid += PrepaidRow(0, payer, receiver, Some(uo.id), uo.cost.getOrElse(0.0f), now, PaymentDao.prepaidPayment))
        }
      })
    } yield(enough)).transactionally
    db.run(actions).map(x => x)
  }

  def userPrepaids(userId: Int) : Future[Seq[(PrepaidRow, Option[String], Option[String])]] = {
    val q = (for {
      ((p, u1), u2) <- Tables.Prepaid join Tables.User on (_.payerId === _.id) join Tables.User on (_._1.receiverId === _.id)
      if (u1.id === userId || u2.id === userId)
    } yield(p, u1.fullName, u2.fullName)).sortBy(x => x._1.transactionDate.desc)
    db.run(q.result).map(x => x)
  }
}

object PaymentDao {
  val prepaidStorage: Short = 0
  val prepaidPayment: Short = 1
}