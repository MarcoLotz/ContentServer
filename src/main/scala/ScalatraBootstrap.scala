import javax.servlet.ServletContext

import com.marcolotz.ContentServer
import com.marcolotz.configuration.ConfigurationManager
import org.scalatra.LifeCycle
/**
  * Created by prometheus on 19/04/2017.
  */
class ScalatraBootstrap extends LifeCycle {

  // Entry point of the application

  // TODO: Command line arguments for diffent
  // TODO: Find a more elegant way to output messages
  println("Loading configuration")

  private val config = new ConfigurationManager().load()

  configureServer()

  override def init(context: ServletContext) {
    // Mount servlets.
    context.mount(new ContentServer, "/content/*")
    context.mount(new LandingServer, "/*")
  }

  def configureServer() =
  {
      // TODO: configure server
  }
}
