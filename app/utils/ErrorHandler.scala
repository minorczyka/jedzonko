package utils

import com.google.inject.Inject
import com.mohiva.play.silhouette.api.SecuredErrorHandler
import controllers.routes
import play.api.{Configuration, OptionalSourceMapper}
import play.api.http.DefaultHttpErrorHandler
import play.api.i18n.{I18nSupport, MessagesApi, Messages}
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results._
import play.api.routing.Router

import scala.concurrent.Future

/**
  * Created by Kompu on 2016-03-11.
  */
class ErrorHandler @Inject() (
  val messagesApi: MessagesApi,
  env: play.api.Environment,
  config: Configuration,
  sourceMapper: OptionalSourceMapper,
  router: javax.inject.Provider[Router])
  extends DefaultHttpErrorHandler(env, config, sourceMapper, router)
  with SecuredErrorHandler with I18nSupport {

  override def onNotAuthenticated(request: RequestHeader, messages: Messages): Option[Future[Result]] =
    Some(Future.successful(Redirect(routes.ApplicationController.signIn())))

  override def onNotAuthorized(request: RequestHeader, messages: Messages): Option[Future[Result]] = {
    Some(Future.successful(Redirect(routes.ApplicationController.signIn()).flashing("error" -> Messages("access.denied")(messages))))
  }
}
