package com.marcolotz

import org.scalatra.ScalatraServlet
import com.marcolotz.filesystem.FileSystemManager
import com.marcolotz.renderer.Renderer

class ContentServlet extends ScalatraServlet {

  get("/") {
    contentType = "text/html"
    val fileList = FileSystemManager.listDir()
    Renderer.renderContentServer(fileList)
  }

  get("/download/:file") {
    val name = params.getOrElse("file", "world")
    "Hello " + name
  }

  get("/stream/:file") {
    val name = params.getOrElse("file", "world")
    "Hello " + name
  }
} 