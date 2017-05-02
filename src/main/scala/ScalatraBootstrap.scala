import javax.servlet.ServletContext

import com.marcolotz.ContentServlet
import com.marcolotz.configuration.ConfigurationManager
import com.marcolotz.filesystem.FileSystemManager
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
    context.mount(new ContentServlet, "/content/*")
    context.mount(new LandingServlet, "/*")
    context.mount(new FileDownloadServlet, "/download/*")
  }

  def configureServer() =
  {
      // TODO: configure server
      FileSystemManager.init(config)
  }
}
