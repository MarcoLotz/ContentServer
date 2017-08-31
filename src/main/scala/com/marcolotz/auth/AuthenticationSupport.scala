package com.marcolotz.auth

import org.scalatra.ScalatraBase
import org.scalatra.auth.strategy.BasicAuthSupport
import org.scalatra.auth.{ScentryConfig, ScentrySupport}

trait AuthenticationSupport extends ScentrySupport[User] with BasicAuthSupport[User] {
  self: ScalatraBase =>

  // All classes that extend this trait have basicAuth before reaching the end-points
  before() {
    basicAuth
  }

  // Used when sendint the authentication header
  val realm = "Content Server"

  protected def fromSession = {
    case id: String => User(id)
  }

  protected def toSession = {
    case usr: User => usr.id
  }

  protected val scentryConfig = (new ScentryConfig {}).asInstanceOf[ScentryConfiguration]

  override protected def configureScentry = {
    scentry.unauthenticated {
      scentry.strategies("Basic").unauthenticated()
    }
  }

  override protected def registerAuthStrategies = {
    scentry.register("Basic", app => new ContentServerAuthStrategy(app, realm))
  }

}