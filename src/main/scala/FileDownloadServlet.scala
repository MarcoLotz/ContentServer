import java.io.File
import java.util.Properties

import com.marcolotz.filesystem.FileSystemManager
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FilenameUtils
import org.scalatra.ScalatraServlet

class FileDownloadServlet extends ScalatraServlet with LazyLogging {

  // TODO: Probably this is better in the constructor of an object called FileDownloadServlet
  // Check if this happens every single time the servlet runs
  // File with known extensions and equivalent response types
  private val properties: Properties = new Properties()

  logger.debug("Loading extensions")
  properties.load(classOf[FileDownloadServlet].getResourceAsStream("extensions.properties"))

  def resolveContentType(resourcePath: String): String = {
    val file = new File(resourcePath)
    if (file.isDirectory) "application/zip"
    else {
      val extension = Option(properties.get(FilenameUtils.getExtension(resourcePath)))
      extension match {
        case Some(ex) => ex.toString
        case None => "application/octet-stream"
      }
    }
  }

  // TODO: Change 404 page
  // TODO: Check for large files
  get("/*") {
    val fileId = params.getOrElse("fileId", {
      // there's no such resource
      logger.debug("empty value for the parameter fileId.")
      halt(404)
    })

    val fileSystemItem = FileSystemManager.findFileByItem(fileId.toInt)

    fileSystemItem match {
      case Some(fsItem) =>
        logger.debug("Download request of valid explored file.")

        val optServedFile = Option(new java.io.File(fsItem.absolutePath))
        optServedFile match {
          case Some(servedFile) => {
            logger.debug("File is being downloaded: " + fsItem)
            contentType = resolveContentType(fileId)

            if (servedFile.isDirectory) {
              response.setHeader("Content-Disposition",
                "attachment; filename=" + servedFile.getName()
                  + ".zip")
              // TODO: Should a tmp response be sent?
              FileSystemManager.getCompressedDirectory(fsItem)
            }
            else {
              response.setHeader("Content-Disposition",
                "attachment; filename=" + servedFile.getName)
              servedFile
            }
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
