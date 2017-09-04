import javax.servlet.ServletContext

import com.marcolotz.contentserver.configuration.ConfigurationManager
import com.marcolotz.contentserver.filesystem.FileSystemManager
import com.marcolotz.contentserver.servlets.{ContentServlet, FileDownloadServlet, LandingServlet, StreamServlet}
import com.typesafe.scalalogging.LazyLogging
import org.scalatra.LifeCycle

/**
  * Created by Marco Lotz on 19/04/2017.
  */
class ScalatraBootstrap extends LifeCycle with LazyLogging {

  override def init(context: ServletContext) {
    configure(context)

    // Mount servlets.
    context.mount(new LandingServlet, "/*")
    context.mount(new ContentServlet, "/content/*")
    context.mount(new FileDownloadServlet, "/download/*")
    context.mount(new StreamServlet, "/stream/*")
  }

  private def configure(context: ServletContext): Unit = {
    // TODO: Add HTTPS:
    // context.initParameters("org.scalatra.ForceHttps") = "true"

    ConfigurationManager.loadAsContainer()
    FileSystemManager.init(ConfigurationManager.getConguration())
  }
}
