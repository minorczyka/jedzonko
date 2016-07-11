package dao

import com.google.inject.{Inject, Singleton}
import models.{PlaceCommentRow, PlaceRow, Tables}
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._
import silhouette.SilhouetteUser
import slick.driver.JdbcProfile

import scala.concurrent.Future

@Singleton
class PlaceDao @Inject()(val dbConfigProvider: DatabaseConfigProvider) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]
  val db = dbConfig.db
  import Tables.profile.api._

  def insertPlace(place: PlaceRow) : Future[PlaceRow] = {
    db.run((Tables.Place returning Tables.Place) += place)
  }

  def updatePlace(place: PlaceRow) : Future[PlaceRow] = {
    val q = for {
      p <- Tables.Place if p.id === place.id
    } yield (p.groupId, p.authorId, p.name, p.url, p.minimumOrder, p.deliveryCost, p.isDeleted)
    db.run(q.update(place.groupId, place.authorId, place.name, place.url, place.minimumOrder, place.deliveryCost, place.isDeleted)).map(_ => place)
  }

  def find(placeId: Int) : Future[Option[PlaceRow]] = {
    val q = Tables.Place.filter(_.id === placeId)
    db.run(q.result.headOption).map(p => p)
  }

  def findUserPlaces(user: SilhouetteUser) : Future[Seq[PlaceRow]] = {
    val q = Tables.Place.filter(_.authorId === user.userId).filter(!_.isDeleted)
    db.run(q.result).map(p => p)
  }

  def findGroupPlaces(groupId: Int) : Future[Seq[PlaceRow]] = {
    val q = Tables.Place.filter(_.groupId === groupId).filter(!_.isDeleted)
    db.run(q.result).map(p => p)
  }

  def insertComment(comment: PlaceCommentRow) : Future[PlaceCommentRow] = {
    db.run((Tables.PlaceComment returning Tables.PlaceComment) += comment).map(c => c)
  }

  def findPlaceComments(placeId: Int) : Future[Seq[(models.UserRow, models.PlaceCommentRow)]] = {
    val q = for {
      (c, u) <- Tables.PlaceComment join Tables.User on (_.authorId === _.id)
      if (c.placeId === placeId)
    } yield (u, c)
    db.run(q.result).map(r => r)
  }

  def allPlaces : Future[Seq[PlaceRow]] = {
    db.run(Tables.Place.result).map(x => x)
  }
}
