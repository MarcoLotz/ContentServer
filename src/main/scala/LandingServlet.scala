import com.marcolotz.renderer.Renderer
import org.scalatra.ScalatraServlet

class LandingServlet extends ScalatraServlet {
  get("/") {
    contentType = "text/html"
    Renderer.renderLandingServer()
  }
} 