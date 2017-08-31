package com.marcolotz.contentserver.filesystem

import java.io.File
import java.nio.file.Files

import com.marcolotz.contentserver.renderer.ContentServerTest
import org.scalatest.BeforeAndAfterAll

/**
  * Created by Marco Lotz on 01/08/2017.
  */
class FileSystemManagerTest extends ContentServerTest with BeforeAndAfterAll {

  val testResourcesPath = new File(getClass.getResource("/").getFile)
  val testDirPath = new File(getClass.getResource("/innerDir").getFile)

  val conf: ServerConfiguration = ServerConfiguration(
    mountPath = testResourcesPath.getAbsolutePath,
    tempDirectory = "tmp",
    showHiddenFiles = true,
    filteredoutExtensions = List("hpptest"))

  override def beforeAll() {
    FileSystemManager.init(conf)
    super.beforeAll()
  }

  override def afterAll() {
    cleanStream()
    super.afterAll()
  }

  private def cleanStream(): Unit = {
    val content = new File("stream-content/")

    // Remove symbolic links
    val links = content.listFiles().map(_.toPath).filter(Files.isSymbolicLink).
      map(Files.delete)
  }

  test("Test filesystem initialization") {
    assert(FileSystemManager.rootFile.absolutePath === testResourcesPath.getAbsolutePath)
    assert(FileSystemManager.rootPath === testResourcesPath.getAbsolutePath)
    assert(FileSystemManager.showHiddenFiles === true)
    assert(FileSystemManager.filteredoutExtensions.size === 1)
  }

  test("Test filesystem filtering") {
    assert(FileSystemManager.filteredoutExtensions.count(item =>
      item.equals("hpptest")) === 1)
    assert(FileSystemManager.discoveredFSItems.count(item =>
      item._2.extension == "hpptest") === 0)
    assert(FileSystemManager.discoveredFSItems.count(item =>
      item._2.extension == "test") === 2)
  }

  // TODO: remove ignore
  ignore("test hidden files") {
    val files = FileSystemManager.discoveredFSItems.map(item => item._2).
      map(file => file.name).toList
    assert(files.contains("hidden"))
  }

  test("Test filesystem discovery content") {
    val files = FileSystemManager.discoveredFSItems.map(item => item._2).
      map(file => file.name).toList
    assert(files.contains("innerDir") &&
      files.contains("file1.test") &&
      files.contains("file2.test") &&
      files.contains("file4.mp4"))
  }

  test("Test list directory") {
    val list = FileSystemManager.listDirectory(FileSystemItemFactory(testDirPath)).map(_.name)
    assert(list.contains("file4.mp4"))
    assert(list.size === 1)
  }

  test("Test generate relative path") {
    val path = FileSystemManager.generateRelativePathFromRoot("test/documents")
    assert(path === "test/documents")
  }

  test("Test stream link generation") {
    val streamFile = getFileFromResources("/innerDir/file4.mp4")
    val file = FileSystemManager.generateStreamLink(streamFile)

    assert(file.exists())
    assert(file.getName.contains("mp4"))
    assert(file.getAbsolutePath.contains("stream-content/file4.mp4"))
  }

  ignore("Test get compressed directory") {

  }
}
