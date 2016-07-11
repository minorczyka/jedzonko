package controllers

import java.sql.Timestamp

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import dao.{GroupDao, PlaceDao, UserDao}
import forms.{CreatePlaceForm, ChatMessageForm}
import models.{PlaceCommentRow, PlaceRow}
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import silhouette.SilhouetteUser

import scala.concurrent.Future

class PlaceController @Inject()(
  val messagesApi: MessagesApi,
  val env: Environment[SilhouetteUser, CookieAuthenticator],
  val groupDao: GroupDao,
  val placeDao: PlaceDao,
  val userDao: UserDao) extends Silhouette[SilhouetteUser, CookieAuthenticator] {

  private def canUserViewPlace(user: SilhouetteUser, placeId: Int) : Future[Option[PlaceRow]] = {
    placeDao.find(placeId).flatMap {
      case Some(place) => {
        groupDao.findGroupMembers(place.groupId).map { members =>
          if (members.exists(m => m.id == user.userId)) Some(place) else None
        }
      }
      case None => Future.successful(None)
    }
  }

  private def doUserOwnsPlace(user: SilhouetteUser, placeId: Int) : Future[Option[PlaceRow]] = {
    placeDao.find(placeId).flatMap {
      case Some(place) => {
        if (place.authorId == user.userId) {
          Future.successful(Some(place))
        } else {
          groupDao.find(place.groupId).map {
            case Some(group) => {
              if (group.ownerId == user.userId) Some(place) else None
            }
            case None => None
          }
        }
      }
      case None => Future.successful(None)
    }
  }

  def createPlace = SecuredAction.async { implicit request =>
    groupDao.findUserGroups(request.identity).map { groups =>
      val select = groups.map(g => (g.id.toString, g.name))
      Ok(views.html.place.createPlace(request.identity, select, CreatePlaceForm.form))
    }
  }

  def handleCreatePlace = SecuredAction.async { implicit request =>
    CreatePlaceForm.form.bindFromRequest.fold(
      form => {
        groupDao.findUserGroups(request.identity).map { groups =>
          val select = groups.map(g => (g.id.toString, g.name))
          BadRequest(views.html.place.createPlace(request.identity, select, form))
        }
      },
      data => {
        groupDao.findUserGroups(request.identity).flatMap { groups =>
          if (groups.exists(g => g.id == data.groupId)) {
            placeDao.insertPlace(PlaceRow(
              0,
              data.groupId,
              request.identity.userId,
              data.name,
              data.url,
              data.minimumOrder,
              data.deliveryCost
            )).map(_ => Redirect(routes.PlaceController.listPlaces()))
          } else {
            Future.successful(NotFound(views.html.place.placeNotFound(request.identity)))
          }
        }
      }
    )
  }

  def listPlaces = SecuredAction.async { implicit request =>
    placeDao.findUserPlaces(request.identity).map(places =>
      Ok(views.html.place.listPlaces(request.identity, places))
    )
  }

  def placeDetails(placeId: Int) = SecuredAction.async { implicit request =>
    canUserViewPlace(request.identity, placeId).flatMap {
      case Some(place) => {
        groupDao.find(place.groupId).flatMap {
          case Some(group) => {
            placeDao.findPlaceComments(placeId).flatMap (comments =>
              userDao.find(place.authorId).map {
                case Some(placeOwner) => Ok(views.html.place.placeDetails(request.identity, place, group, placeOwner, comments))
                case None => NotFound(views.html.place.placeNotFound(request.identity))
              }
            )
          }
          case None => Future.successful(NotFound(views.html.place.placeNotFound(request.identity)))
        }
      }
      case None => Future.successful(NotFound(views.html.place.placeNotFound(request.identity)))
    }
  }

  def editPlace(placeId: Int) = SecuredAction.async { implicit request =>
    doUserOwnsPlace(request.identity, placeId).flatMap {
      case Some(place) => {
        groupDao.find(place.groupId).map {
          case Some(group) => Ok(views.html.place.editPlace(request.identity, place, group, CreatePlaceForm.form))
          case None => NotFound(views.html.place.placeNotFound(request.identity))
        }
      }
      case None => Future.successful(NotFound(views.html.place.placeNotFound(request.identity)))
    }
  }

  def handleEditPlace(placeId: Int) = SecuredAction.async { implicit request =>
    doUserOwnsPlace(request.identity, placeId).flatMap {
      case Some(place) => {
        CreatePlaceForm.form.bindFromRequest.fold(
          form => {
            groupDao.find(place.groupId).map {
              case Some(group) => BadRequest(views.html.place.editPlace(request.identity, place, group, form))
              case None => NotFound(views.html.place.placeNotFound(request.identity))
            }
          },
          data => {
            placeDao.updatePlace(place.copy(
              url = data.url,
              minimumOrder = data.minimumOrder,
              deliveryCost = data.deliveryCost)).map(place => Redirect(routes.PlaceController.placeDetails(placeId)))
          }
        )
      }
      case None => Future.successful(NotFound(views.html.place.placeNotFound(request.identity)))
    }
  }

  def deletePlace(placeId: Int) = SecuredAction.async { implicit request =>
    doUserOwnsPlace(request.identity, placeId).flatMap {
      case Some(place) => {
        placeDao.updatePlace(place.copy(isDeleted = true)).map(place => Redirect(routes.PlaceController.listPlaces))
      }
      case None => Future.successful(NotFound(views.html.place.placeNotFound(request.identity)))
    }
  }

  def handleAddComment(placeId: Int) = SecuredAction.async { implicit request =>
    ChatMessageForm.form.bindFromRequest.fold(
      form => Future.successful(Redirect(routes.PlaceController.placeDetails(placeId))),
      data => {
        placeDao.insertComment(PlaceCommentRow(
          0,
          request.identity.userId,
          placeId,
          data.message,
          new Timestamp(System.currentTimeMillis)
        )).map(_ => Redirect(routes.PlaceController.placeDetails(placeId)))
      }
    )
  }
}
