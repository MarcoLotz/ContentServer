import java.io._
import java.util.Properties

import com.marcolotz.filesystem.FileSystemManager
import com.sun.xml.internal.ws.developer.ServerSideException
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

  // TODO: Change 404 page
  get("/:id") {
    def serveFile(file: File): Unit = {
      val outputStream: OutputStream = response.getOutputStream();
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

    val fileId = params.getOrElse("id", {
      // there's no such resource
      logger.debug("empty value for the parameter fileId.")
      halt(404)
    })

    val fileSystemItem = FileSystemManager.getFileByItemId(fileId.toInt)

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
