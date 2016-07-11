package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers.SocialProviderRegistry
import dao.{GroupDao, PlaceDao, UserDao}
import forms.ChangeGroupPassForm
import forms.{CreateGroupForm, FindGroupForm, JoinGroupForm}
import models.GroupRow
import play.api.i18n.{Messages, MessagesApi}
import play.api.libs.concurrent.Execution.Implicits._
import silhouette.SilhouetteUser

import scala.concurrent.Future

class GroupController @Inject()(
  val messagesApi: MessagesApi,
  val env: Environment[SilhouetteUser, CookieAuthenticator],
  val groupDao: GroupDao,
  val placeDao: PlaceDao) extends Silhouette[SilhouetteUser, CookieAuthenticator] {

  def createGroup = SecuredAction { implicit request =>
    Ok(views.html.group.createGroup(request.identity, CreateGroupForm.form))
  }

  def handleCreateGroup = SecuredAction.async { implicit request =>
    CreateGroupForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.group.createGroup(request.identity, form))),
      data => {
        groupDao.findByName(data.groupName).flatMap {
          case Some(group) => {
            val form = CreateGroupForm.form.withError("groupName", Messages("group.create.exists"))
            Future.successful(BadRequest(views.html.group.createGroup(request.identity, form)))
          }
          case None => {
            groupDao.createGroup(data.groupName, data.groupPassword1, request.identity).map(_ =>
              Redirect(routes.GroupController.listGroups())
            )
          }
        }
      }
    )
  }

  def listGroups = SecuredAction.async { implicit request =>
    groupDao.findUserGroupsWithOwners(request.identity).map { groups =>
      Ok(views.html.group.listGroups(request.identity, groups))
    }
  }

  def groupDetails(groupId: Int) = SecuredAction.async { implicit request =>
    groupDao.find(groupId).flatMap {
      case Some(group) => {
        groupDao.findGroupMembers(group.id).flatMap(members =>
          if (members.exists(u => u.id == request.identity.userId)) {
            placeDao.findGroupPlaces(group.id).map(places =>
              Ok(views.html.group.groupDetails(request.identity, group, members, places))
            )
          } else {
            Future.successful(Redirect(routes.GroupController.joinGroup(group.id)))
          }
        )
      }
      case None => Future.successful(NotFound(views.html.group.groupNotFound(request.identity)))
    }
  }

  def joinGroup(groupId: Int) = SecuredAction.async { implicit request =>
    groupDao.find(groupId).flatMap {
      case Some(group) => {
        groupDao.findGroupMembers(group.id).map(members =>
          if (members.exists(u => u.id == request.identity.userId)) {
            Redirect(routes.GroupController.groupDetails(group.id))
          } else {
            Ok(views.html.group.joinGroup(request.identity, group, JoinGroupForm.form))
          }
        )
      }
      case None => Future.successful(NotFound(views.html.group.groupNotFound(request.identity)))
    }
  }

  def handleJoinGroup(groupId: Int) = SecuredAction.async { implicit request =>
    groupDao.find(groupId).flatMap {
      case Some(group) => {
        JoinGroupForm.form.bindFromRequest.fold(
          form => Future.successful(BadRequest(views.html.group.joinGroup(request.identity, group, form))),
          data => {
            if (data.groupPassword.equals(group.password)) {
              groupDao.addUserToGroup(group, request.identity.userId).map(_ =>
                Redirect(routes.GroupController.groupDetails(group.id))
              )
            } else {
              val form = JoinGroupForm.form.withError("groupPassword", Messages("group.join.wrong"))
              Future.successful(BadRequest(views.html.group.joinGroup(request.identity, group, form)))
            }
          }
        )
      }
      case None => Future.successful(NotFound(views.html.group.groupNotFound(request.identity)))
    }
  }

  def deleteGroup(groupId: Int) = SecuredAction.async { implicit request =>
    groupDao.find(groupId).map {
      case Some(group) => {
        if (group.ownerId != request.identity.userId) {
          Redirect(routes.GroupController.groupDetails(group.id))
        } else {
          Ok(views.html.group.deleteGroup(request.identity, group, JoinGroupForm.form))
        }
      }
      case None => NotFound(views.html.group.groupNotFound(request.identity))
    }
  }

  def handleDeleteGroup(groupId: Int) = SecuredAction.async { implicit request =>
    groupDao.find(groupId).flatMap {
      case Some(group) => {
        if (group.ownerId == request.identity.userId) {
          JoinGroupForm.form.bindFromRequest.fold(
            form => Future.successful(BadRequest(views.html.group.joinGroup(request.identity, group, form))),
            data => {
              if (data.groupPassword.equals(group.password)) {
                groupDao.deleteGroup(group.id).map(_ =>
                  Redirect(routes.GroupController.listGroups())
                )
              } else {
                val form = JoinGroupForm.form.withError("groupPassword", Messages("group.join.wrong"))
                Future.successful(BadRequest(views.html.group.deleteGroup(request.identity, group, form)))
              }
            }
          )
        } else {
          Future.successful(NotFound(views.html.group.groupNotFound(request.identity)))
        }
      }
      case None => Future.successful(NotFound(views.html.group.groupNotFound(request.identity)))
    }
  }

  def changePassword(groupId: Int) = SecuredAction.async { implicit request =>
    groupDao.find(groupId).map {
      case Some(group) => {
        if (group.ownerId == request.identity.userId) {
          Ok(views.html.group.changeGroupPassword(request.identity, group, ChangeGroupPassForm.form))
        } else {
          NotFound(views.html.group.groupNotFound(request.identity))
        }
      }
      case None => NotFound(views.html.group.groupNotFound(request.identity))
    }
  }

  def handleChangePassword(groupId: Int) = SecuredAction.async { implicit request =>
    groupDao.find(groupId).flatMap {
      case Some(group) => {
        ChangeGroupPassForm.form.bindFromRequest.fold(
          form => Future.successful(BadRequest(views.html.group.changeGroupPassword(request.identity, group, form))),
          data => {
            if (group.password == data.oldPassword) {
              groupDao.updateGroup(group.copy(password = data.groupPassword1)).map(_ => Redirect(routes.GroupController.groupDetails(group.id)))
            } else {
              Future.successful(Redirect(routes.GroupController.changePassword(group.id)).flashing("error" -> Messages("auth.invalid.credentials")))
            }
          }
        )
      }
      case None => Future.successful(NotFound(views.html.group.groupNotFound(request.identity)))
    }
  }

  def leaveGroup(groupId: Int) = SecuredAction.async { implicit request =>
    groupDao.removeUserFromGroup(groupId, request.identity.userId).map(_ => Redirect(routes.GroupController.listGroups))
  }

  def findGroup = SecuredAction { implicit request =>
    Ok(views.html.group.findGroup(request.identity, FindGroupForm.form))
  }

  def handleFindGroup = SecuredAction.async { implicit request =>
    FindGroupForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.group.findGroup(request.identity, form))),
      data => {
        groupDao.querryName(data.groupName).map(groups =>
          Ok(views.html.group.findGroupResults(request.identity, groups))
        )
      }
    )
  }

  def kickUser(groupId: Int, userId: Int) = SecuredAction.async { implicit request =>
    groupDao.find(groupId).flatMap {
      case Some(group) => {
        if (group.ownerId == request.identity.userId) {
          groupDao.removeUserFromGroup(groupId, userId).map(_ => Redirect(routes.GroupController.groupDetails(groupId)))
        } else {
          Future.successful(NotFound(views.html.group.groupNotFound(request.identity)))
        }
      }
      case None => Future.successful(NotFound(views.html.group.groupNotFound(request.identity)))
    }
  }
}
