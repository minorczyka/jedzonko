package models.daos

import java.util.UUID

import com.google.inject.{Inject, Singleton}
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.{PasswordHasher, PasswordInfo}
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import models.{PasswordInfoRow, Tables}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.concurrent.Future

/**
  * The DAO to store the password information.
  */
@Singleton
class PasswordInfoDao @Inject()(val dbConfigProvider: DatabaseConfigProvider, val passwordHasher: PasswordHasher) extends DelegableAuthInfoDAO[PasswordInfo] {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import Tables.profile.api._

  /**
    * Finds the auth info which is linked with the specified login info.
    *
    * @param loginInfo The linked login info.
    * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
    */
  def find(loginInfo: LoginInfo) : Future[Option[PasswordInfo]] = {
    val q = for {
      (p, u) <- Tables.PasswordInfo join Tables.User on(_.userId === _.id)
      if (u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey)
    } yield p
    db.run(q.result.headOption).map(passOption =>
      passOption.map(pass =>
        PasswordInfo(pass.hasher, pass.password, pass.salt)
      )
    )
  }

  /**
    * Adds new auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be added.
    * @param authInfo The auth info to add.
    * @return The added auth info.
    */
  def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val q = Tables.User.filter(u => u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey)
    val actions = (for {
      u <- q.result.head
      _ <- Tables.PasswordInfo += PasswordInfoRow(0, authInfo.hasher, authInfo.password, authInfo.salt, None, u.id)
    } yield()).transactionally
    db.run(actions).map(_ => authInfo)
  }

  /**
    * Updates the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be updated.
    * @param authInfo The auth info to update.
    * @return The updated auth info.
    */
  def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    val q = Tables.PasswordInfo.filter(_.userId in Tables.User.filter(u => u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey).map(_.id))
    db.run(q.map(p => (p.hasher, p.password, p.salt))
      .update((authInfo.hasher, authInfo.password, authInfo.salt)))
      .map(_ => authInfo)
  }

  /**
    * Saves the auth info for the given login info.
    *
    * This method either adds the auth info if it doesn't exists or it updates the auth info
    * if it already exists.
    *
    * @param loginInfo The login info for which the auth info should be saved.
    * @param authInfo The auth info to save.
    * @return The saved auth info.
    */
  def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
    find(loginInfo).flatMap {
      case Some(_) => update(loginInfo, authInfo)
      case None => add(loginInfo, authInfo)
    }
  }

  /**
    * Removes the auth info for the given login info.
    *
    * @param loginInfo The login info for which the auth info should be removed.
    * @return A future to wait for the process to be completed.
    */
  def remove(loginInfo: LoginInfo): Future[Unit] = {
    val q = for {
      (p, u) <- Tables.PasswordInfo join Tables.User on(_.userId === _.id)
      if (u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey)
    } yield p
    db.run(q.delete).map(_ => ())
  }

  def createToken(loginInfo: LoginInfo): Future[String] = {
    val q = Tables.PasswordInfo.filter(_.userId in Tables.User.filter(u => u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey).map(_.id))
    val token = UUID.randomUUID().toString
    db.run(q.map(p => p.resetToken)
      .update(Some(token)))
      .map(_ => token)
  }

  def retrieveToken(token: String): Future[Option[LoginInfo]] = {
    val q = for {
      (p, u) <- Tables.PasswordInfo join Tables.User on(_.userId === _.id)
      if (p.resetToken === token)
    } yield u
    db.run(q.result.headOption).map(userOption =>
      userOption.map(user => LoginInfo(user.providerId, user.providerKey))
    )
  }
  def consume(token: String, newPass: String): Unit = {
    val passInfo = passwordHasher.hash(newPass)
    val q = Tables.PasswordInfo.filter(_.resetToken === token)
    db.run(q.map(p => (p.password, p.hasher, p.salt, p.resetToken))
      .update(passInfo.password, passInfo.hasher, passInfo.salt, None))
      .map(_ => ())
  }
}