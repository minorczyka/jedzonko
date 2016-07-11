package dao

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import models.{Oauth2InfoRow, Tables}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._
import slick.driver.JdbcProfile

import scala.collection.mutable
import scala.concurrent.Future

/**
  * The DAO to store the OAuth2 information.
  *
  * Note: Not thread safe, demo only.
  */
class OAuth2InfoDao @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends DelegableAuthInfoDAO[OAuth2Info] {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import Tables.profile.api._

  /**
    * Finds the auth info which is linked with the specified login info.
    *
    * @param loginInfo The linked login info.
    * @return The retrieved auth info or None if no auth info could be retrieved for the given login info.
    */
  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    val q = for {
      (o, u) <- Tables.Oauth2Info join Tables.User on (_.userId === _.id)
      if (u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey)
    } yield o
    db.run(q.result.headOption).map(oauthOption =>
      oauthOption.map(oauth =>
        OAuth2Info(oauth.accessToken, oauth.tokenType, oauth.expiresIn, oauth.refreshToken)
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
  def add(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    val q = Tables.User.filter(u => u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey)
    val actions = (for {
      u <- q.result.head
      _ <- Tables.Oauth2Info += Oauth2InfoRow(0, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken, u.id)
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
  def update(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    val q = Tables.Oauth2Info.filter(_.userId in Tables.User.filter(u => u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey).map(_.id))
    db.run(q.map(o => (o.accessToken, o.tokenType, o.expiresIn, o.refreshToken))
      .update((authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken)))
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
  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
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
      (o, u) <- Tables.Oauth2Info join Tables.User on(_.userId === _.id)
      if (u.providerId === loginInfo.providerID && u.providerKey === loginInfo.providerKey)
    } yield o
    db.run(q.delete).map(_ => ())
  }
}