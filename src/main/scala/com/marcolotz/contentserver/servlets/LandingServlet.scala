package com.marcolotz.contentserver.servlets

import com.marcolotz.contentserver.auth.AuthenticationSupport
import com.marcolotz.contentserver.renderer.Renderer
import com.marcolotz.contentserver.request.ResponseHandler
import com.typesafe.scalalogging.LazyLogging
import org.scalatra.ScalatraServlet

class LandingServlet extends ScalatraServlet with AuthenticationSupport with LazyLogging{

  /** *
    * Custom implemented to handle css and javascript objects
    */
  // TODO: make this also for video
  before() {
    contentType = ResponseHandler.resolveDefaultRequestResponse(request)
  }

  get("/") {
    Renderer.renderLandingServer()
  }
}
