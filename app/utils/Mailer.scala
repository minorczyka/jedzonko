package utils

import play.api.i18n.Messages
import play.twirl.api.Html
import silhouette.SilhouetteUser

object Mailer {

  implicit def html2String(html: Html): String = html.toString

  def forgotPassword(email: String, link: String)(implicit ms: MailService, m: Messages) {
    ms.sendEmailAsync(email)(
      subject = Messages("mail.forgotpwd.subject"),
      bodyHtml = views.html.mails.forgotPassword(email, link),
      bodyText = views.html.mails.forgotPasswordTxt(email, link)
    )
  }

}
