package com.marcolotz.filesystem

import java.io.File
import java.nio.file.NotDirectoryException

import com.marcolotz.configuration.ServerConfiguration
import com.typesafe.scalalogging.LazyLogging

/**
  * Created by prometheus on 20/04/2017.
  */
// TODO: Write unit tests for this
object FileSystemManager extends LazyLogging {

  /** *
    * Mount entry point for the content server
    */
  var rootPath: String = ""

  /***
    * File at the mount entry point
    */
  var rootFile: FileSystemItem = null

  // Options from the configuration files
  /***
    * Enables lambda to filter or not hidden files (files starting with '.')
    */
  var showHiddenFiles = false
  /** *
    * Filters out a given type of extension
    */
  var filterExtensions = false

  /***
    * Pre analyses all the possible files on init, instead of requiring the user to
    * access that dir before being able to download it.
    */
  var preemptiveFileSystemExploration = true

  /***
    * Stores all the reported items
    */
  var discoveredFSItems = Map[Int, FileSystemItem]()

  // Filtering functions:
  val hiddenFile = (x: FileSystemItem) => if (!showHiddenFiles) !x.name.startsWith(".") else true

  // TODO: add extension filtering
  val filteredExtension = (x: FileSystemItem) => if (filterExtensions) !x.name.startsWith(".") else true

  //val filteringFunctions = Array[(FileSystemItem) => Boolean](hiddenFile _,
  //                              filteringFunctions _)

  @throws[NotDirectoryException]
  def init(conf: ServerConfiguration) = {
    rootPath = conf.mountPath
    showHiddenFiles = conf.showHiddenFiles
    preemptiveFileSystemExploration = conf.preemptiveFileSystemExploration

    val specifiedRootFile = new java.io.File(rootPath)

    if (!specifiedRootFile.exists && !specifiedRootFile.isDirectory) {
      throw new NotDirectoryException("file " + specifiedRootFile.getAbsolutePath + " is not a directory")
    }
    else {
      rootFile = FileSystemItemFactory(specifiedRootFile)
    }

    if (preemptiveFileSystemExploration) recursivelyExploreFS()
  }

  //TODO: Don't allow paths that try to go back or are more than a folder away
  private def validPath(item: FileSystemItem): Boolean = {
    true
  }

  /***
    * generate Map with the files that were just discovered
    * @param items
    * @return
    */
  private def reportItems(items: List[File]): Map[Int, FileSystemItem] = {
    val reportedMap: Map[Int, FileSystemItem] = items.map(item => FileSystemItemFactory(item)).map(item => item.hashCode() -> item).toMap

    // TODO: Apply filters here

    // Log the final output
    reportedMap.foreach(entry => logger.debug("Item being reported: " + entry._2.name + " Path: " + entry._2.absolutePath))
    reportedMap
  }

  /***
    * Explore the files in the directory
    * @param topDir
    * @return a Map of reported files in the directory
    */
  private def exploreDirectory(topDir: FileSystemItem): Map[Int, FileSystemItem] = {
    val dir = new java.io.File(topDir.absolutePath)
    if (dir.exists() && dir.isDirectory) {
      val items = reportItems(dir.listFiles().toList)

      // It may be the first time visiting the directory
      if (!preemptiveFileSystemExploration)
        {
          // TODO: Solve the problem of exploring the directory multiple times
          discoveredFSItems = List(discoveredFSItems, items).flatten.toMap
        }
      items
    }
    else Map()
  }

  /***
    * Lists the content of a directory
    * @param topDir
    * @return
    */
  def listDirectory(topDir: FileSystemItem): List[FileSystemItem] = {
    exploreDirectory(topDir).map(entry => entry._2) toList
  }

  /***
    * Explores the file system recursively
    * @return Map of all the files under the top directory
    */
  def recursivelyExploreFS() {

    def recursiveExploreFS(dirList: List[FileSystemItem], reportedItemsAcc: Map[Int, FileSystemItem]): Map[Int, FileSystemItem] = {
      if (dirList.isEmpty) reportedItemsAcc
      else {
        val newReportedFiles = exploreDirectory(dirList.head)
        val directoriesToExplore = dirList.tail ::: newReportedFiles.filter(entry => entry._2.isDirectory).map(entry => entry._2).toList

        recursiveExploreFS(directoriesToExplore, List(reportedItemsAcc, newReportedFiles).flatten.toMap)
      }
    }
    logger.debug("File system exploration enabled.")

    discoveredFSItems = recursiveExploreFS(List(rootFile), Map())
  }

  /***
    * Translates a file ID to a specific file
    * @param fileId
    * @return
    */
  def getFileFromId(fileId: Int): Option[FileSystemItem] = discoveredFSItems.get(fileId)

  /***
    * Finds FileSystemItem by fileid
    * @param fileId
    * @return
    */
  def findFileByItem(fileId: Int): Option[FileSystemItem] = Option(discoveredFSItems.getOrElse(fileId, null))

}
