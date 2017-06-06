import java.io._
import java.util.Properties

import com.marcolotz.filesystem.FileSystemManager
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.{FilenameUtils, IOUtils}
import org.scalatra.{NotFound, ScalatraServlet}

class FileDownloadServlet extends ScalatraServlet with LazyLogging {

  // Load known extensions
  private val properties: Properties = new Properties()
  properties.load(classOf[FileDownloadServlet].getResourceAsStream("extensions.properties"))

  private def resolveContentType(resourcePath: String): String = {
    val file = new File(resourcePath)
    if (file.isDirectory) "application/zip"
    else {
      val extension = Option(properties.get(FilenameUtils.getExtension(resourcePath).toLowerCase()))
      extension match {
        case Some(ex) => ex.toString
        case None => "application/octet-stream"
      }
    }
  }

  private def serveFile(file: File): Unit = {
    val outputStream: OutputStream = response.getOutputStream()
    val inputStream: FileInputStream = new FileInputStream(file)
    try {
      if (file.length() > Int.MaxValue) {
        IOUtils.copyLarge(inputStream, outputStream)
      }
      else {
        IOUtils.copy(inputStream, outputStream)
      }
    }
    catch {
      case ioe: IOException => logger.error("Exception serving file: "
        + file.getAbsolutePath + " " + ioe.toString)
    }
    finally {
      inputStream.close()
      outputStream.close()
    }
  }

  // TODO: Change 404 page
  get("/:id") {

    FileSystemManager.getFileByItemId(util.Try(params("id").toInt).getOrElse(0)) match {

      case Some(fsItem) =>
        logger.debug("Content download has been requested")

        val optServedFile = Option(new java.io.File(fsItem.absolutePath))

        optServedFile match {
          case Some(servedFile) => {
            logger.debug("File is being downloaded: " + fsItem.name)
            contentType = resolveContentType(fsItem.absolutePath)

            if (servedFile.isDirectory) {
              response.setHeader("Content-Disposition",
                "attachment; filename=" + servedFile.getName()
                  + ".zip")
              val compressedDir = FileSystemManager.getCompressedDirectory(fsItem)
              compressedDir match {
                case Some(file) => serveFile(file)
                // File could not be compressed
                case None => NotFound("Error compressing file")
              }
            }
            else {
              response.setHeader("Content-Disposition",
                "attachment; filename=" + servedFile.getName)
              serveFile(servedFile)
            }
          }
          case None => {
            logger.debug("Content could not be provided as byte stream")
            halt(500)
          }
        }
      case None => {
        logger.debug("Invalid content download requested")
        NotFound("Requested content could not be found")
      }
    }
  }
}
