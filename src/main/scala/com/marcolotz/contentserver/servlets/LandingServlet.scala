package com.marcolotz.contentserver.servlets

import com.marcolotz.contentserver.auth.AuthenticationSupport
import com.marcolotz.contentserver.renderer.Renderer
import org.scalatra.ScalatraServlet

class LandingServlet extends ScalatraServlet with AuthenticationSupport {

  before() {
    contentType = "text/html"
  }

  get("/") {
    Renderer.renderLandingServer()
  }
}
