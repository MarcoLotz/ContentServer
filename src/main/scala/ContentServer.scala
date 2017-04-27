package com.marcolotz

import org.scalatra.ScalatraServlet
import com.marcolotz.filesystem.FileSystemManager
import com.marcolotz.renderer.Renderer

class ContentServer extends ScalatraServlet {

  // TODO: Change path
  val fsManager = new FileSystemManager("/Users/prometheus/Desktop/projects/unipop/unipop")

  get("/") {
    contentType = "text/html"
    val fileList = fsManager.listDir()
    Renderer.renderContentServer(fileList)
  }

  get("/download/*") {
    val name = params.getOrElse("name", "world")
    "Hello " + name
  }

  get("/stream/*") {
    val name = params.getOrElse("name", "world")
    "Hello " + name
  }
} 