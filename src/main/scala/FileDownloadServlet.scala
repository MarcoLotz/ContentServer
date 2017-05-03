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
    val extension = Option(properties.get(FilenameUtils.getExtension(resourcePath)))

    extension match {
      case Some(ex) => ex.toString
      case None => "application/octet-stream"
    }
  }

  // TODO: Change 404 page
  get("/*") {
    val fileId = params.getOrElse("fileId", {
      // there's no such resource
      logger.debug("empty value for the parameter fileId.")
      halt(404)
    })

    val filePath = FileSystemManager.findFileByItem(fileId.toInt)

    filePath match {
      case Some(file) =>
        logger.debug("Download request of valid explored file.")

        val servedFile = Option(new java.io.File(file.absolutePath))

        servedFile match {
          case Some(file) => {
            // TODO: zip if it is a directory
            logger.debug("File is being downloaded: " + file)
            contentType = resolveContentType(fileId)
            response.setHeader("Content-Disposition", "attachment; filename=" + file.getName)
            file
          }
          case None => {
            logger.debug("File could not be provided as byte stream")
            halt(404)
          }
        }
      case None => {
        logger.debug("The file has not been explored yet. Is it a direct access?")
        halt(404)
      }
    }
  }
}