import com.marcolotz.auth.AuthenticationSupport
import com.marcolotz.renderer.Renderer
import org.scalatra.ScalatraServlet

class LandingServlet extends ScalatraServlet with AuthenticationSupport {

  before() {
    contentType = "text/html"
    // TODO: Fix authentication
    //basicAuth
  }

  get("/") {
    Renderer.renderLandingServer()
  }
}
