package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.{LoginEvent, LoginInfo, Silhouette, Environment}
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.{PasswordInfo, PasswordHasher}
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.exceptions.IdentityNotFoundException
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.daos.PasswordInfoDao
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, RequestHeader}
import play.api.libs.concurrent.Execution.Implicits._
import silhouette.SilhouetteUser
import silhouette.service.UserService
import utils.{Mailer, MailService}

import scala.concurrent.Future

/**
  * Created by Kompu on 2016-03-11.
  */
class PasswordChangeController @Inject() (
  val messagesApi: MessagesApi,
  val env: Environment[SilhouetteUser, CookieAuthenticator],
  val userService: UserService,
  val authInfoRepository: AuthInfoRepository,
  val credentialsProvider: CredentialsProvider,
  val avatarService: AvatarService,
  val passwordHasher: PasswordHasher,
  val mailService: MailService,
  val passwordInfoDao: PasswordInfoDao) extends Silhouette[SilhouetteUser, CookieAuthenticator] {

  implicit val ms = mailService

  private def notFoundDefault (implicit request: RequestHeader) =
    Future.successful(NotFound(views.html.error.notFound(request)))

  def forgotPassword = UserAwareAction.async { implicit request =>
    Future.successful(request.identity match {
      case Some(_) => Redirect(routes.ApplicationController.index())
      case None => Ok(views.html.auth.forgotPassword(emailForm))
    })
  }

  val emailForm = Form(single("email" -> email))

  /**
    * Sends an email to the user with a link to reset the password
    */
  def handleForgotPassword = Action.async { implicit request =>
    emailForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.auth.forgotPassword(formWithErrors))),
      email => env.identityService.retrieve(LoginInfo("credentials", email)).flatMap {
        case Some(_) => {
          passwordInfoDao.createToken(LoginInfo("credentials", email)).map{ token =>
            Mailer.forgotPassword(email, link = routes.PasswordChangeController.resetPassword(token).absoluteURL())
            Ok(views.html.auth.forgotPasswordSent(email))
          }
        }
        case None => Future.successful(BadRequest(views.html.auth.forgotPassword(emailForm.withError("email", Messages("auth.user.notexists")))))
      }
    )
  }

  val resetPasswordForm = Form(tuple(
    "password1" -> nonEmptyText,
    "password2" -> nonEmptyText
  ) verifying (Messages("auth.passwords.notequal"), passwords => passwords._2 == passwords._1))

  /**
    * Confirms the user's link based on the token and shows him a form to reset the password
    */
  def resetPassword(token: String) = Action.async { implicit request =>
    passwordInfoDao.retrieveToken(token).flatMap {
      case Some(_) => {
        Future.successful(Ok(views.html.auth.resetPassword(token, resetPasswordForm)))
      }
      case None => notFoundDefault
    }
  }

  /**
    * Saves the new password and authenticates the user
    */
  def handleResetPassword(token: String) = Action.async { implicit request =>
    resetPasswordForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.auth.resetPassword(token, formWithErrors))),
      passwords => {
        passwordInfoDao.retrieveToken(token).flatMap {
          case Some(loginInfo) => {
            env.identityService.retrieve(loginInfo).flatMap {
              case Some(user) => {
                for {
                  authenticator <- env.authenticatorService.create(loginInfo)
                  result <- env.authenticatorService.renew(authenticator, Ok(views.html.auth.resetedPassword(user)))
                } yield {
                  passwordInfoDao.consume(token, passwords._1)
                  env.eventBus.publish(LoginEvent(user, request, request2Messages))
                  result
                }
              }
              case None => Future.failed(new IdentityNotFoundException("Couldn't find user"))
            }
          }
          case None => notFoundDefault
        }
      }
    )
  }
}
