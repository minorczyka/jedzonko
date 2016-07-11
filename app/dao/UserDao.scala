package dao

import java.util.UUID

import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.LoginInfo
import models.Tables
import models.UserRow
import play.api.db.slick.DatabaseConfigProvider
import silhouette.SilhouetteUser
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

@Singleton
class UserDao @Inject()(val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import Tables.profile.api._

  def all : Future[Seq[UserRow]] = {
    db.run(Tables.User.result)
  }

  def insertUser(user: UserRow) : Future[UserRow] = {
    db.run((Tables.User returning Tables.User) += user)
  }

  def countUserGroups(userId: Int) : Future[Int] = {
    val q = Tables.UserToGroup.filter(_.userId === userId).length
    db.run(q.result).map(x => x)
  }

  def countUserPlaces(userId: Int) : Future[Int] = {
    val q = Tables.Place.filter(_.authorId === userId).length
    db.run(q.result).map(x => x)
  }

  def countUserVotings(userId: Int) : Future[Int] = {
    val q = Tables.Voting.filter(_.authorId === userId).length
    db.run(q.result).map(x => x)
  }

  def countUserOrders(userId: Int) : Future[Int] = {
    val q = Tables.Order.filter(_.authorId === userId).length
    db.run(q.result).map(x => x)
  }

  def countUserIncomes(userId: Int) : Future[Int] = {
    val q = (for {
      ((o, uo), u) <- Tables.Order join Tables.UserToOrder on (_.id === _.orderId) join Tables.User on (_._2.userId === _.id)
      if (o.authorId === userId && o.status === OrderDao.orderFinished && !(uo.userId === userId) && uo.status === OrderDao.orderNotPayed)
    } yield()).length
    db.run(q.result).map(x => x)
  }

  def countUserCharges(userId: Int) : Future[Int] = {
    val q = (for {
      ((uo, o), u) <- Tables.UserToOrder join Tables.Order on (_.orderId === _.id) join Tables.User on (_._2.authorId === _.id)
      if (uo.userId === userId && o.status === OrderDao.orderFinished && !(o.authorId === userId) && uo.status === OrderDao.orderNotPayed)
    } yield()).length
    db.run(q.result).map(x => x)
  }

  def countUserAvailableVotings(userId: Int) : Future[Int] = {
    val q = (for {
      (((u, ug), g), v) <- Tables.User join Tables.UserToGroup on (_.id === _.userId) join Tables.Group on (_._2.groupId === _.id) join Tables.Voting on (_._2.id === _.groupId)
      if (u.id === userId && v.status === VotingDao.votingStarted)
    } yield()).length
    db.run(q.result).map(x => x)
  }

  def countUserAvailableOrders(userId: Int) : Future[Int] = {
    val q = (for {
      (((u, ug), g), o) <- Tables.User join Tables.UserToGroup on (_.id === _.userId) join Tables.Group on (_._2.groupId === _.id) join Tables.Order on (_._2.id === _.groupId)
      if (u.id === userId && o.status === OrderDao.orderStarted)
    } yield()).length
    db.run(q.result).map(x => x)
  }

  def allUsers : Future[Seq[UserRow]] = {
    db.run(Tables.User.result).map(x => x)
  }

  // Silhouette
  def find(loginInfo: LoginInfo) : Future[Option[SilhouetteUser]] = {
    val q = Tables.User.filter(u => u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey)
    db.run(q.result.headOption).map(userOption =>
      userOption.map(user =>
        SilhouetteUser(
          user.id,
          LoginInfo(user.providerId, user.providerKey),
          user.firstName,
          user.lastName,
          user.fullName,
          user.email,
          user.avatarUrl,
          user.privileges)
      )
    )
  }

  def find(userId: Int) : Future[Option[SilhouetteUser]] = {
    val q = Tables.User.filter(u => u.id === userId)
    db.run(q.result.headOption).map(userOption =>
      userOption.map(user =>
        SilhouetteUser(
          user.id,
          LoginInfo(user.providerId, user.providerKey),
          user.firstName,
          user.lastName,
          user.fullName,
          user.email,
          user.avatarUrl,
          user.privileges)
      )
    )
  }

  def findUser(userId: Int) : Future[Option[UserRow]] = {
    val q = Tables.User.filter(u => u.id === userId)
    db.run(q.result.headOption).map(u => u)
  }

  def save(user: SilhouetteUser) = {
    val q = Tables.User.filter(u => u.id === user.userId)
    val actions = (for {
      userOption <- q.result.headOption
      _ <- Tables.User.insertOrUpdate(userOption.getOrElse(UserRow(
        user.userId,
        user.loginInfo.providerID,
        user.loginInfo.providerKey,
        user.firstName,
        user.lastName,
        user.fullName,
        user.email,
        user.avatarURL,
        user.privileges
      )))
    } yield()).transactionally
    db.run(actions).map(_ => user)
  }
}

object UserDao {
  val normalUser: Short = 0
  val admin: Short = 1
}