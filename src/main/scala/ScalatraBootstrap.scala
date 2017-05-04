import javax.servlet.ServletContext

import com.marcolotz.ContentServlet
import com.marcolotz.configuration.ConfigurationManager
import com.marcolotz.filesystem.FileSystemManager
import com.typesafe.scalalogging.LazyLogging
import org.scalatra.LifeCycle

/**
  * Created by prometheus on 19/04/2017.
  */
class ScalatraBootstrap extends LifeCycle with LazyLogging {

  // Entry point of the application

  // TODO: Command line arguments to bypass Json config.
  // TODO: Find a more elegant way to output messages

  logger.info("Loading configuration")

  private val config = new ConfigurationManager().load()
  configureServer()

  override def init(context: ServletContext) {
    // Register authentications strategies on sentry. The sequence is cascaded

    context.initParameters("scentry.strategies") =
      "com.marcolotz.auth.ContentServerAuthenticationStrategy"

    // Mount servlets.
    context.mount(new LandingServlet, "/*")
    context.mount(new ContentServlet, "/content/*")
    context.mount(new FileDownloadServlet, "/download/*")
  }

  private def configureServer() = {
    // TODO: configure server
    FileSystemManager.init(config)
  }
}
