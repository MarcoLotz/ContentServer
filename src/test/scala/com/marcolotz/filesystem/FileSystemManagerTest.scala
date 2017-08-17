package com.marcolotz.filesystem

import java.io.File

import com.marcolotz.configuration.ServerConfiguration
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.io.Source

/**
  * Created by Marco Lotz on 01/08/2017.
  */
class FileSystemManagerTest extends FunSuite with BeforeAndAfterAll {

  val testResourcesPath = new File(getClass.getResource("/").getFile)
  val testDirPath = new File(getClass.getResource("/innerDir").getFile)

  val conf: ServerConfiguration = ServerConfiguration(
    mountPath = testResourcesPath.getAbsolutePath,
    tempDirectory = testResourcesPath.getAbsolutePath + "/tmp",
    showHiddenFiles = true,
    filteredoutExtensions = List("hpptest"))

  override def beforeAll() {
    FileSystemManager.init(conf)
    super.beforeAll()
  }

  override def afterAll() {
    super.afterAll()
  }

  test("Test Filesystem Initialization"){
    assert(FileSystemManager.rootFile.absolutePath === testResourcesPath.getAbsolutePath)
    assert(FileSystemManager.rootPath === testResourcesPath.getAbsolutePath)
    assert(FileSystemManager.showHiddenFiles === true)
    assert(FileSystemManager.filteredoutExtensions.size === 1)
  }

  test("Test Filesystem filtering"){
    assert(FileSystemManager.filteredoutExtensions.count(item =>
      item.equals("hpptest")) === 1)
    assert(FileSystemManager.discoveredFSItems.count(item =>
      item._2.extension == "hpptest") === 0)
    assert(FileSystemManager.discoveredFSItems.count(item =>
      item._2.extension == "test") === 2)
  }

  ignore ("test hidden files"){
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
    // TODO: the list should contain hidden assert(list.contains(""))
    assert(list.size === 1)
  }
}
