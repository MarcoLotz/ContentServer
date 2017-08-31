package com.marcolotz.contentserver.auth

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import com.marcolotz.contentserver.configuration.ConfigurationManager
import org.scalatra.ScalatraBase
import org.scalatra.auth.strategy.BasicAuthStrategy

class ContentServerAuthStrategy(protected override val app: ScalatraBase, realm: String)
  extends BasicAuthStrategy[User](app, realm) {

  val conf = ConfigurationManager.getConguration()

  val expectedUsername = if (conf.username.isEmpty) "scalatra" else conf.username
  val expectedPassword = if (conf.password.isEmpty) "scalatra" else conf.password

  override protected def getUserId(user: User)(implicit request: HttpServletRequest,
                                               response: HttpServletResponse): String = user.id

  override protected def validate(userName: String, password: String)(implicit request:
  HttpServletRequest, response: HttpServletResponse): Option[User] = {
    if (userName == expectedUsername && password == expectedPassword) Some(User(expectedUsername))
    else None
  }
}