import com.marcolotz.renderer.Renderer
import org.scalatra.ScalatraServlet

class LandingServlet extends ScalatraServlet {
  get("/") {
    contentType = "text/html"
    Renderer.renderLandingServer()
  }

  get("/:name") {
    val name = params.getOrElse("name", "world")
    "Hello " + name
  }
} 