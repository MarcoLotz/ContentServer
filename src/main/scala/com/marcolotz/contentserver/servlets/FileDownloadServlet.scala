package com.marcolotz.contentserver.servlets

import java.io._

import com.marcolotz.contentserver.auth.AuthenticationSupport
import com.marcolotz.contentserver.filesystem.FileSystemManager
import com.marcolotz.contentserver.request.ResponseHandler
import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.IOUtils
import org.scalatra.{InternalServerError, NotFound, Ok, ScalatraServlet}

class FileDownloadServlet extends ScalatraServlet with AuthenticationSupport with LazyLogging {

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
            contentType = ResponseHandler.resolveContentType(fsItem.absolutePath)

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
              Ok(serveFile(servedFile))
            }
          }
          case None => {
            logger.debug("Content could not be provided as byte stream")
            InternalServerError("Content could not be provided as byte stream")
          }
        }
      case None => {
        logger.debug("Invalid content download requested")
        NotFound("Requested content could not be found")
      }
    }
  }
}
