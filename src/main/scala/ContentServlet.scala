package com.marcolotz

import com.marcolotz.auth.AuthenticationSupport
import com.marcolotz.filesystem.FileSystemManager
import com.marcolotz.renderer.Renderer
import com.typesafe.scalalogging.LazyLogging
import org.scalatra.ScalatraServlet


/** *
  * Content server. FileId are used instead of paths, in order to
  * prevent the user from having server-side filesystem information.
  */
class ContentServlet extends ScalatraServlet with AuthenticationSupport with LazyLogging {

  before(){
    contentType = "text/html"
  }

  get("/") {
    val fileList = FileSystemManager.listDirectory(FileSystemManager.rootFile)
    Renderer.renderContentServer(fileList, FileSystemManager.rootFile)
  }

  /** *
    * shows the content of a given directory, using its file ID
    */
  get("/directory") {
    val fileId = params.getOrElse("fileId", {
      // there's no such resource
      logger.debug("empty value for the parameter fileId.")
      halt(404)
    }).toInt

    val topDirectory = FileSystemManager.getFileFromId(fileId).getOrElse({
      logger.debug("file system item id could not be found in the reported items")
      halt(404)
    })

    val fileList = FileSystemManager.listDirectory(topDirectory)

    Renderer.renderContentServer(fileList, topDirectory)
  }

  get("/stream") {
    val name = params.getOrElse("file", "Marco")
    "Hello " + name
  }
}
