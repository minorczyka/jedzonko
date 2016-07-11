package dao

import java.sql.Timestamp

import com.google.inject.{Inject, Singleton}
import models.{GroupRow, Tables, UserRow, UserToGroupRow}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._
import silhouette.SilhouetteUser
import slick.driver.JdbcProfile

import scala.concurrent.Future

@Singleton
class GroupDao @Inject()(val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import Tables.profile.api._

  def createGroup(name: String, pass: String, author: SilhouetteUser) : Future[GroupRow] = {
    val now = new Timestamp(System.currentTimeMillis)
    val actions = (for {
      g <- (Tables.Group returning Tables.Group) += GroupRow(0, author.userId, name, pass, now)
      _ <- Tables.UserToGroup += UserToGroupRow(0, author.userId, g.id)
    } yield(g)).transactionally
    db.run(actions).map(g => g)
  }

  def updateGroup(group: GroupRow) : Future[GroupRow] = {
    val q = Tables.Group.filter(_.id === group.id)
    db.run(q.update(group)).map(_ => group)
  }

  def deleteGroup(groupId: Int) : Future[Unit] = {
    val q = Tables.Group.filter(_.id === groupId)
    db.run(q.delete).map(_ => ())
  }

  def find(groupId: Int) : Future[Option[GroupRow]] = {
    val q = Tables.Group.filter(_.id === groupId)
    db.run(q.result.headOption).map(g => g)
  }

  def findByName(name: String) : Future[Option[GroupRow]] = {
    val q = Tables.Group.filter(_.name === name)
    db.run(q.result.headOption).map(g => g)
  }

  def querryName(querry: String) : Future[Seq[(GroupRow, UserRow)]] = {
    val q = for {
      (g, u) <- Tables.Group join Tables.User on (_.ownerId === _.id)
      if (g.name.toLowerCase like (querry.toLowerCase + "%"))
    } yield(g, u)
    db.run(q.take(25).result).map(g => g)
  }

  def findUserGroups(user: SilhouetteUser) : Future[Seq[GroupRow]] = {
    val q = for {
      ((u, ug), g) <- Tables.User join Tables.UserToGroup on (_.id === _.userId) join Tables.Group on (_._2.groupId === _.id)
      if (u.id === user.userId)
    } yield(g)
    db.run(q.result).map(groups => groups)
  }

  def findUserGroupsWithOwners(user: SilhouetteUser) : Future[Seq[(GroupRow, UserRow)]] = {
    val q = for {
      (((u, ug), g), o) <- Tables.User join Tables.UserToGroup on (_.id === _.userId) join Tables.Group on (_._2.groupId === _.id) join Tables.User on (_._2.ownerId === _.id)
      if (u.id === user.userId)
    } yield(g, o)
    db.run(q.result).map(groups => groups)
  }

  def findGroupMembers(groupId: Int) : Future[Seq[UserRow]] = {
    val q = for {
      ((u, ug), g) <- Tables.User join Tables.UserToGroup on (_.id === _.userId) join Tables.Group on (_._2.groupId === _.id)
      if (g.id === groupId)
    } yield(u)
    db.run(q.result).map(users => users)
  }

  def addUserToGroup(group: GroupRow, userId: Int) : Future[Unit] = {
    db.run(Tables.UserToGroup += UserToGroupRow(0, userId, group.id)).map(_ => ())
  }

  def removeUserFromGroup(groupId: Int, userId: Int) : Future[Unit] = {
    val q = Tables.UserToGroup.filter(x => x.userId === userId && x.groupId === groupId)
    db.run(q.delete).map(_ => ())
  }

  def allGroups : Future[Seq[GroupRow]] = {
    db.run(Tables.Group.result).map(x => x)
  }
}
