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

  override def init(context: ServletContext) {
    configure
    // Register authentications strategies on sentry. The sequence is cascaded
    context.initParameters("scentry.strategies") =
      "com.marcolotz.auth.ContentServerAuthenticationStrategy"

    // Mount servlets.
    context.mount(new LandingServlet, "/*")
    context.mount(new ContentServlet, "/content/*")
    context.mount(new FileDownloadServlet, "/download/*")
    context.mount(new StreamServlet, "/stream/*")
  }

  private def configure(): Unit = {
    ConfigurationManager.loadAsContainer()
    FileSystemManager.init(ConfigurationManager.getConguration())
  }
}
