package controllers

import com.google.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.services.AvatarService
import com.mohiva.play.silhouette.api.util.PasswordHasher
import com.mohiva.play.silhouette.impl.authenticators.CookieAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import forms.SignUpForm
import play.api.i18n.{ MessagesApi, Messages }
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Action
import silhouette.SilhouetteUser
import silhouette.service.UserService

import scala.concurrent.Future

/**
  * The sign up controller.
  *
  * @param messagesApi The Play messages API.
  * @param env The Silhouette environment.
  * @param userService The user service implementation.
  * @param authInfoRepository The auth info repository implementation.
  * @param avatarService The avatar service implementation.
  * @param passwordHasher The password hasher implementation.
  */
class SignUpController @Inject() (val messagesApi: MessagesApi,
                                  val env: Environment[SilhouetteUser, CookieAuthenticator],
                                  val userService: UserService,
                                  val authInfoRepository: AuthInfoRepository,
                                  val avatarService: AvatarService,
                                  val passwordHasher: PasswordHasher)
  extends Silhouette[SilhouetteUser, CookieAuthenticator] {

  /**
    * Registers a new user.
    *
    * @return The result to display.
    */
  def signUp = Action.async { implicit request =>
    SignUpForm.form.bindFromRequest.fold(
      form => Future.successful(BadRequest(views.html.signUp(form))),
      data => {
        val loginInfo = LoginInfo(CredentialsProvider.ID, data.email)
        userService.retrieve(loginInfo).flatMap {
          case Some(user) =>
            Future.successful(Redirect(routes.ApplicationController.signUp()).flashing("error" -> Messages("auth.signup.exists")))
          case None =>
            val authInfo = passwordHasher.hash(data.password1)
            val user = SilhouetteUser(
              userId = 0,
              loginInfo = loginInfo,
              firstName = Some(data.firstName),
              lastName = Some(data.lastName),
              fullName = Some(data.firstName + " " + data.lastName),
              email = Some(data.email),
              avatarURL = None,
              privileges = SilhouetteUser.NormalUser
            )
            for {
              avatar <- avatarService.retrieveURL(data.email)
              user <- userService.save(user.copy(avatarURL = avatar))
              authInfo <- authInfoRepository.add(loginInfo, authInfo)
              authenticator <- env.authenticatorService.create(loginInfo)
              value <- env.authenticatorService.init(authenticator)
              result <- env.authenticatorService.embed(value, Redirect(routes.ApplicationController.index()))
            } yield {
              env.eventBus.publish(SignUpEvent(user, request, request2Messages))
              env.eventBus.publish(LoginEvent(user, request, request2Messages))
              result
            }
        }
      }
    )
  }
}