import com.marcolotz.configuration.ConfigurationManager
import com.marcolotz.filesystem.FileSystemManager
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.DefaultServlet
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

/**
  * Created by prometheus on 15/05/2017.
  */
object JettyLauncher extends App with LazyLogging {
  override def main(args: Array[String]): Unit = {

    configureServer(args)

    val port = ConfigurationManager.getConguration().port

    val server = new Server(port)
    val context = new WebAppContext()

    context.setContextPath("/")
    context.setResourceBase("/src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    server.setHandler(context)
    server.start
    server.join
  }

  private def configureServer(args: Array[String]) = {
    try {
      ConfigurationManager.load(args = args)
    }
    catch {
      case e: IllegalArgumentException => System.exit(1)
      case unkown => unkown.printStackTrace()
    }
  }
}
