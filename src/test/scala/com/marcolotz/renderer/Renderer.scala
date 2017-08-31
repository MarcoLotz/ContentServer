package com.marcolotz.renderer

import java.io.File

import com.marcolotz.ContentServerTest
import com.marcolotz.contentserver.filesystem.FileSystemItemFactory
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

/**
  * Created by Marco Lotzon 01/08/2017.
  */
@RunWith(classOf[JUnitRunner])
class RendererTest extends ContentServerTest{

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
