import java.util.Properties

import com.marcolotz.filesystem.{FileSystemItem, FileSystemManager}
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.{FilenameUtils, IOUtils}
import org.scalatra.ScalatraServlet

class FileDownloadServlet extends ScalatraServlet with LazyLogging {

  // TODO: Probably this is better in the constructor of an object called FileDownloadServlet
  // Check if this happens every single time the servlet runs
  // File with known extensions and equivalent response types
  private val properties: Properties = new Properties()

  logger.debug("Loading extensions")
  properties.load(classOf[FileDownloadServlet].getResourceAsStream("extensions.properties"))

  def resolveContentType(resourcePath: String) = {
    val extension = properties.get(FilenameUtils.getExtension(resourcePath))
    // TODO: Check if this is the best solution if the extension is not found
    if (extension != null) extension.toString else "text/plain"
  }


  get("/:fileId") {
    val fileId = params.getOrElse("fileId", "")

    // there's no such resource
    if (fileId.equals("")) {
      logger.debug("empty value for the parameter fileId.")
      halt(404)
    }
    else {

      val filePath = FileSystemManager.findFileByItem(fileId.toInt)

      filePath match {
        case Some(file) =>
          Option(servletContext.getResourceAsStream(file.absolutePath)) match {

            // File already discovered by the file manager
            case Some(inputStream) => {
              logger.debug("File is being downloaded: " + file)

              val fileContent = IOUtils.toByteArray(inputStream)

              // find out content type for headers
              contentType = resolveContentType(fileId)
              fileContent
            }
            // Return resource not found (404)
            case None => halt(404)
          }
        case None => {
          logger.debug("The file has not been explored yet. Is it a direct access?")
          halt(404)
        }
      }
    }
  }
}