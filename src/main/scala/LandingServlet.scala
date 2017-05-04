import com.marcolotz.auth.AuthenticationSupport
import com.marcolotz.renderer.Renderer
import org.scalatra.ScalatraServlet

class LandingServlet extends ScalatraServlet with AuthenticationSupport {
  get("/") {
    basicAuth
    contentType = "text/html"
    Renderer.renderLandingServer()
  }
}
