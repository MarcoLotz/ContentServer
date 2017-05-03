package com.marcolotz

import org.scalatra.ScalatraServlet
import com.marcolotz.filesystem.FileSystemManager
import com.marcolotz.renderer.Renderer
import com.typesafe.scalalogging.LazyLogging


/***
  * Content server. Fileid are used instead of paths, in order to
  * prevent the user from having server-side filesystem information.
  */
class ContentServlet extends ScalatraServlet with LazyLogging {

  get("/") {
    contentType = "text/html"
    val fileList = FileSystemManager.listRootPath()
    Renderer.renderContentServer(fileList, FileSystemManager.rootFile)
  }

  /***
    * shows the content of a given directory, using its file ID
    */
  get("/directory") {
    contentType = "text/html"

    val fileId = params.getOrElse("fileId", {
      // there's no such resource
      logger.debug("empty value for the parameter fileId.")
      halt(404)
    }).toInt

    val fileList = FileSystemManager.exploreFileSystemItem(fileId)
    // TODO: Solve signature
    Renderer.renderContentServer(fileList, FileSystemManager.discoveredFSItems.get(fileId).get)
  }

  get("/stream") {
    val name = params.getOrElse("file", "Marco")
    "Hello " + name
  }
} 