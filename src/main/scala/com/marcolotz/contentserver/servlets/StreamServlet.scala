package com.marcolotz.contentserver.servlets

import com.marcolotz.contentserver.auth.AuthenticationSupport
import com.marcolotz.contentserver.filesystem.{FileSystemItem, FileSystemManager}
import com.marcolotz.contentserver.renderer.Renderer
import com.typesafe.scalalogging.LazyLogging
import org.scalatra.{InternalServerError, NotFound, ScalatraServlet}


/** *
  * Stream servlet. FileId are used instead of paths, in order to
  * prevent the user from having server-side filesystem information.
  */
class StreamServlet extends ScalatraServlet with AuthenticationSupport with LazyLogging {

  before() {
    contentType = "text/html"
  }

  get("/:id") {
    FileSystemManager.getFileByItemId(util.Try(params("id").toInt).getOrElse(0)) match {
      case Some(requestedFile) => {
        if (requestedFile.isPlayable) {
          provideStream(requestedFile)
        }
        else {
          logger.debug("Requested item is not watchable")
          NotFound("Requested item is not watchable")
        }
      }
      case None => {
        logger.debug("watchable file item id could not be found in the reported items")
        NotFound("Sorry, the file could not be found")
      }
    }
  }

  private def provideStream(file: FileSystemItem) = {
    val stream = FileSystemManager.generateStreamLink(file)
    if (stream.exists()) {
      Renderer.renderStream(file)
    }
    else {
      InternalServerError("Could not provide stream file")
    }
  }
}
