package com.marcolotz.auth

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.scalatra.ScalatraBase
import org.scalatra.auth.strategy.BasicAuthStrategy

/**
  * Created by prometheus on 04/05/2017.
  */
class ContentServerAuthenticationStrategy(protected override val app: ScalatraBase, realm: String)
  extends BasicAuthStrategy[User](app, realm) {

  /** *
    * Attempts to login a User using the methods on a User model
    *
    * @param userName
    * @param password
    * @return
    */
  protected def validate(userName: String, password: String)
                        (implicit request: HttpServletRequest, response: HttpServletResponse)
  : Option[User] = {
    if (userName == "scalatra" && password == "scalatra") Some(User("scalatra"))
    else None
  }

  protected def getUserId(user: User)
                         (implicit request: HttpServletRequest, response: HttpServletResponse)
  : String = user.id
}