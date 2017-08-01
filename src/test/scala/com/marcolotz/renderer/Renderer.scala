package com.marcolotz.renderer

import java.io.File

import com.marcolotz.filesystem.{FileSystemItem, FileSystemItemFactory}
import org.scalatest.{BeforeAndAfterAll, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

import scala.io.Source

/**
  * Created by Marco Lotzon 01/08/2017.
  */
@RunWith(classOf[JUnitRunner])
class RendererTest extends FunSuite{

  private def getFileFromResources(path: String): FileSystemItem =
    FileSystemItemFactory(new File(getClass.getResource(path).getFile))

  test("rendered welcome page")(assert(Renderer.renderLandingServer.contains("html")))

  test("Render stream file"){
    val streamFile = getFileFromResources("/innerDir/file4.mp4")
    assert(streamFile.isPlayable === true)
    assert(Renderer.
      renderStream(streamFile).
      contains("video"))
  }

  test("Render content server"){
    val topFile = getFileFromResources("/innerDir")
    val fileList = List(getFileFromResources("/innerDir/file4.mp4"))

    assert(Renderer.renderContentServer(fileList, topFile).toLowerCase().contains("file4.mp4"))
    assert(Renderer.renderContentServer(fileList, topFile).toLowerCase().contains("innerdir"))
  }
}
