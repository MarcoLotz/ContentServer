import com.marcolotz.auth.AuthenticationSupport
import com.marcolotz.renderer.Renderer
import org.scalatra.ScalatraServlet

class LandingServlet extends ScalatraServlet with AuthenticationSupport {

  before(){
    contentType = "text/html"
  }

  get("/") {
    //basicAuth
    Renderer.renderLandingServer()
  }
}
