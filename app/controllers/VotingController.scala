package controllers

import java.sql.Timestamp

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{Environment, Silhouette}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import dao.{GroupDao, PlaceDao, UserDao, VotingDao}
import forms.{CreateVotingForm, VoteForm}
import models.VotingRow
import play.api.i18n.MessagesApi
import play.api.libs.concurrent.Execution.Implicits._
import silhouette.SilhouetteUser

import scala.concurrent.Future

class VotingController  @Inject()(
  val messagesApi: MessagesApi,
  val env: Environment[SilhouetteUser, CookieAuthenticator],
  val groupDao: GroupDao,
  val votingDao: VotingDao,
  val placeDao: PlaceDao,
  val userDao: UserDao) extends Silhouette[SilhouetteUser, CookieAuthenticator] {

  private def canUserViewVoting(user: SilhouetteUser, votingId: Int) : Future[Option[VotingRow]] = {
    votingDao.findUserVotings(user.userId).map { votings =>
      votings.filter(v => v._2.id == votingId).headOption.map(x => x._2)
    }
  }

  private def doUserOwnsVoting(user: SilhouetteUser, votingId: Int) : Future[Option[VotingRow]] = {
    votingDao.find(votingId).map {
      case Some(voting) => {
        if (voting.authorId == user.userId) Some(voting) else None
      }
      case None => None
    }
  }

  def createVoting = SecuredAction.async { implicit request =>
    groupDao.findUserGroups(request.identity).map { groups =>
      val select = groups.map(g => (g.id.toString, g.name))
      Ok(views.html.vote.createVoting(request.identity, select, CreateVotingForm.form))
    }
  }

  def handleCreateVoting = SecuredAction.async { implicit request =>
    CreateVotingForm.form.bindFromRequest.fold(
      form => {
        groupDao.findUserGroups(request.identity).map { groups =>
          val select = groups.map(g => (g.id.toString, g.name))
          BadRequest(views.html.vote.createVoting(request.identity, select, form))
        }
      },
      data => {
        groupDao.findUserGroups(request.identity).flatMap { groups =>
          if (groups.exists(g => g.id == data.groupId)) {
            votingDao.insertVoting(VotingRow(
              0,
              data.groupId,
              request.identity.userId,
              VotingDao.votingStarted,
              new Timestamp(System.currentTimeMillis),
              data.votingEnd.map(d => new Timestamp(d.getTime)),
              data.orderEnd.map(d => new Timestamp(d.getTime))
            )).flatMap { voting =>
              votingDao.insertVotingPlaces(voting, data.placesIds).map(_ => Redirect(routes.VotingController.listVotings))
            }
          } else {
            Future.successful(NotFound(views.html.place.placeNotFound(request.identity)))
          }
        }
      }
    )
  }

  def listVotings = SecuredAction.async { implicit request =>
    votingDao.findUserVotingsWithAuthors(request.identity.userId).map(votings =>
      Ok(views.html.vote.listVotings(request.identity, votings))
    )
  }

  def votingDetails(votingId: Int) = SecuredAction.async { implicit request =>
    canUserViewVoting(request.identity, votingId).flatMap {
      case Some(voting) => votingDao.findVotingPlaces(votingId).flatMap(places =>
        userDao.findUser(voting.authorId).flatMap {
          case Some(author) => {
            votingDao.findVotes(votingId).map(votes =>
              Ok(views.html.vote.votingDetails(request.identity, voting, places, author, votes, VoteForm.form))
            )
          }
          case None => Future.successful(NotFound(views.html.vote.votingNotFound(request.identity)))
        }
      )
      case None => Future.successful(NotFound(views.html.vote.votingNotFound(request.identity)))
    }
  }

  def editVoting(votingId: Int) = SecuredAction.async { implicit request =>
    doUserOwnsVoting(request.identity, votingId).flatMap {
      case Some(voting) => {
        if (voting.status == VotingDao.votingStarted) {
          groupDao.findUserGroups(request.identity).flatMap { groups =>
            val select = groups.map(g => (g.id.toString, g.name))
            votingDao.findVotingPlaces(votingId).flatMap(chosenPlaces =>
              placeDao.findGroupPlaces(voting.groupId).map(allPlaces =>
                Ok(views.html.vote.editVoting(request.identity, voting, allPlaces, chosenPlaces, select, CreateVotingForm.form))
              )
            )
          }
        } else {
          Future.successful(NotFound(views.html.vote.votingNotFound(request.identity)))
        }
      }
      case None => Future.successful(NotFound(views.html.vote.votingNotFound(request.identity)))
    }
  }

  def handleEditVoting(votingId: Int) = SecuredAction.async { implicit request =>
    doUserOwnsVoting(request.identity, votingId).flatMap {
      case Some(voting) => {
        CreateVotingForm.form.bindFromRequest.fold(
          form => {
            groupDao.findUserGroups(request.identity).flatMap { groups =>
              val select = groups.map(g => (g.id.toString, g.name))
              votingDao.findVotingPlaces(votingId).flatMap(chosenPlaces =>
                placeDao.findGroupPlaces(voting.groupId).map(allPlaces =>
                  BadRequest(views.html.vote.editVoting(request.identity, voting, allPlaces, chosenPlaces, select, form))
                )
              )
            }
          },
          data => {
            if (voting.status == VotingDao.votingStarted) {
              votingDao.updateVoting(voting.copy(
                votingEnd = data.votingEnd.map(d => new Timestamp(d.getTime)),
                orderEnd = data.orderEnd.map(d => new Timestamp(d.getTime)))).flatMap { _ =>
                votingDao.updateVotingPlaces(voting, data.placesIds).map(_ =>
                  Redirect(routes.VotingController.votingDetails(votingId))
                )
              }
            } else {
              Future.successful(NotFound(views.html.vote.votingNotFound(request.identity)))
            }
          }
        )
      }
      case None => Future.successful(NotFound(views.html.vote.votingNotFound(request.identity)))
    }
  }

  def handleVote(votingId: Int) = SecuredAction.async { implicit request =>
    canUserViewVoting(request.identity, votingId).flatMap {
      case Some(voting) => {
        if (voting.status == VotingDao.votingStarted) {
          VoteForm.form.bindFromRequest.fold(
            form => votingDao.findVotingPlaces(votingId).flatMap(places =>
              userDao.findUser(voting.authorId).flatMap {
                case Some(author) => {
                  votingDao.findVotes(votingId).map(votes =>
                    BadRequest(views.html.vote.votingDetails(request.identity, voting, places, author, votes, form))
                  )
                }
                case None => Future.successful(NotFound(views.html.vote.votingNotFound(request.identity)))
              }
            ),
            data => {
              votingDao.updateUserVotes(votingId, request.identity.userId, data.placesIds).map(_ =>
                Redirect(routes.VotingController.votingDetails(votingId))
              )
            }
          )
        } else {
          Future.successful(NotFound(views.html.vote.votingNotFound(request.identity)))
        }
      }
      case None => Future.successful(NotFound(views.html.vote.votingNotFound(request.identity)))
    }
  }

  def finishVoting(votingId: Int) = SecuredAction.async { implicit request =>
    doUserOwnsVoting(request.identity, votingId).flatMap {
      case Some(voting) => {
        votingDao.updateVoting(voting.copy(status = VotingDao.votingFinished)).map(v => Redirect(
          routes.VotingController.votingDetails(v.id)
        ))
      }
      case None => Future.successful(NotFound(views.html.vote.votingNotFound(request.identity)))
    }
  }

  def deleteVoting(votingId: Int) = SecuredAction.async { implicit request =>
    doUserOwnsVoting(request.identity, votingId).flatMap {
      case Some(voting) => {
        votingDao.deleteVoting(votingId).map(_ => Redirect(routes.VotingController.listVotings))
      }
      case None => Future.successful(NotFound(views.html.vote.votingNotFound(request.identity)))
    }
  }
}
