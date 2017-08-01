import com.marcolotz.configuration.ConfigurationManager
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.{DefaultServlet, ServletHolder}
import org.eclipse.jetty.webapp.WebAppContext
import org.scalatra.servlet.ScalatraListener

/**
  * Created by Marco Lotz on 15/05/2017.
  */
object JettyLauncher extends App with LazyLogging {
  override def main(args: Array[String]): Unit = {

    configureServer(args)

    val port = ConfigurationManager.getConguration().port

    val server = new Server(port)

    val context = new WebAppContext()

    // Add default servlet to "/"
    context.setContextPath("/")
    context.setResourceBase("src/main/webapp")
    context.addEventListener(new ScalatraListener)
    context.addServlet(classOf[DefaultServlet], "/")

    // add special Servelet for "stream-content" mapped directory
    val streamServlet = new ServletHolder("static-home", classOf[DefaultServlet])
    streamServlet.setInitParameter("resourceBase",
      "stream-content")
    streamServlet.setInitParameter("dirAllowed", "true")
    streamServlet.setInitParameter("pathInfoOnly", "true")
    context.addServlet(streamServlet, "/stream-content/*")

    // Start server
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
      case unknown => unknown.printStackTrace()
    }
  }
}
