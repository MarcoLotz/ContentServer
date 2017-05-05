package com.marcolotz.auth

import org.scalatra.ScalatraBase
import org.scalatra.auth.strategy.BasicAuthSupport
import org.scalatra.auth.{ScentryConfig, ScentrySupport}

trait AuthenticationSupport extends ScentrySupport[User] with BasicAuthSupport[User] {

  /* Forces the compiler to check if any class in a hierarchy, including AuthenticationSupport
   * is or extends ScalatraBase, so AuthencationSupport can now use fields or methods from
   * ScalatraBase
   */
  self: ScalatraBase =>

  val realm = "Marco Lotz's Content Server"
  protected val scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]

  protected def fromSession = {
    case id: String => User(id)
  }

  protected def toSession = {
    case usr: User => usr.id
  }

  override protected def configureScentry = {
    scentry.unauthenticated {
      scentry.strategies("Basic").unauthenticated()
    }
  }
}
