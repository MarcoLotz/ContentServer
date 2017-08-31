package com.marcolotz.contentserver.servlets

import com.marcolotz.contentserver.auth.AuthenticationSupport
import com.marcolotz.contentserver.filesystem.FileSystemManager
import com.marcolotz.contentserver.renderer.Renderer
import com.typesafe.scalalogging.LazyLogging
import org.scalatra.{NotFound, Ok, ScalatraServlet}


/** *
  * Content server. FileId are used instead of paths, in order to
  * prevent the user from having server-side filesystem information.
  */
class ContentServlet extends ScalatraServlet with AuthenticationSupport with LazyLogging {

  before() {
    contentType = "text/html"
  }

  get("/") {
    val fileList = FileSystemManager.listDirectory(FileSystemManager.rootFile)
    Renderer.renderContentServer(fileList, FileSystemManager.rootFile)
  }

  /** *
    * shows the content of a given directory, using its file ID
    */
  get("/directory/:id") {
    FileSystemManager.getFileByItemId(util.Try(params("id").toInt).getOrElse(0)) match {
      case Some(topDirectory) => {
        val fileList = FileSystemManager.listDirectory(topDirectory)
        Ok(Renderer.renderContentServer(fileList, topDirectory))
      }
      case None => {
        logger.debug("file system item id could not be found in the reported items")
        NotFound("Sorry, the file could not be found")
      }
    }
  }
}
