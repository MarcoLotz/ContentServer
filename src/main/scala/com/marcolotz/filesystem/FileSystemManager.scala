package com.marcolotz.filesystem

import java.io.File
import java.nio.file.NotDirectoryException

import com.marcolotz.configuration.ServerConfiguration
import com.marcolotz.filesystem
import com.typesafe.scalalogging.LazyLogging

/**
  * Created by prometheus on 20/04/2017.
  */
// TODO: Write unit tests for this
object FileSystemManager extends LazyLogging {

  /** *
    * Filtering options enabled in the configuration files
    */
  private object filteringFunctions extends LazyLogging {
    /** *
      * Filters out hidden files from reports when enabled
      *
      * @param item
      * @return
      */
    def removeHiddenFiles(item: FileSystemItem) = if (!FileSystemManager.showHiddenFiles) !item.name.startsWith(".") else true

    /** *
      * Filters out files from a fixed extension
      *
      * @return
      */
    def removeExtensions (item: FileSystemItem) = if (!FileSystemManager.filteredoutExtensions.isEmpty) !FileSystemManager.filteredoutExtensions.contains(item.extension.toLowerCase()) else true

  }

  /** *
    * Mount entry point for the content server
    */
  var rootPath: String = ""

  /** *
    * File at the mount entry point
    */
  var rootFile: FileSystemItem = null

  // Options from the configuration file
  /** *
    * Enables lambda to filter or not hidden files (files starting with '.')
    */
  var showHiddenFiles = false
  /** *
    * Filters out a given type of extension
    */
  var filteredoutExtensions: List[String] = List()

  /** *
    * Pre analyses all the possible files on init, instead of requiring the user to
    * access that dir before being able to download it.
    */
  var preemptiveFileSystemExploration = true

  /***
    * Stores all the reported items
    */
  var discoveredFSItems = Map[Int, FileSystemItem]()

  @throws[NotDirectoryException]
  def init(conf: ServerConfiguration) = {
    rootPath = conf.mountPath
    showHiddenFiles = conf.showHiddenFiles
    preemptiveFileSystemExploration = conf.preemptiveFileSystemExploration
    filteredoutExtensions = conf.filteredoutExtensions.map(x => x.toLowerCase())

    val specifiedRootFile = new java.io.File(rootPath)

    if ((!specifiedRootFile.exists && !specifiedRootFile.isDirectory) ||
        !validPath(specifiedRootFile)){
      throw new NotDirectoryException("file " + specifiedRootFile.getAbsolutePath + " is not a directory")
    }
    else {
      rootFile = FileSystemItemFactory(specifiedRootFile)
    }

    if (preemptiveFileSystemExploration) recursivelyExploreFS()
  }

  /***
    * Prevents relative paths on mount time. Only accepts absolute paths.
    * @param item
    * @return
    */
  private def validPath(item: File): Boolean = item.isAbsolute

  private def applyFilteringFunctions(item: FileSystemItem): Boolean = {
    filteringFunctions.removeExtensions(item) &&
    filteringFunctions.removeHiddenFiles(item)
  }

  /** *
    * generate Map with the files that were just discovered
    *
    * @param items
    * @return
    */
  private def reportItems(items: List[File]): Map[Int, FileSystemItem] = {
    val reportedMap: Map[Int, FileSystemItem] = items.map(item => FileSystemItemFactory(item)).map(item => item.hashCode() -> item).toMap

    // Apply filters
    val filteredMap = reportedMap.filter(item => applyFilteringFunctions(item._2))

    // Log the final output
    filteredMap.foreach(entry => logger.debug("Item being reported: " + entry._2.name + " Path: " + entry._2.absolutePath))
    filteredMap
  }

  /** *
    * Explore the files in the directory
    *
    * @param topDir
    * @return a Map of reported files in the directory
    */
  private def exploreDirectory(topDir: FileSystemItem): Map[Int, FileSystemItem] = {
    val dir = new java.io.File(topDir.absolutePath)
    if (dir.exists() && dir.isDirectory) {
      val items = reportItems(dir.listFiles().toList)

      // It may be the first time visiting the directory
      if (!preemptiveFileSystemExploration) {
        // TODO: Solve the problem of exploring the directory multiple times
        discoveredFSItems = List(discoveredFSItems, items).flatten.toMap
      }
      items
    }
    else Map()
  }

  /** *
    * Lists the content of a directory
    *
    * @param topDir
    * @return
    */
  def listDirectory(topDir: FileSystemItem): List[FileSystemItem] = {
    exploreDirectory(topDir).map(entry => entry._2) toList
  }

  /** *
    * Explores the file system recursively
    *
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

  /** *
    * Translates a file ID to a specific file
    *
    * @param fileId
    * @return
    */
  def getFileFromId(fileId: Int): Option[FileSystemItem] = discoveredFSItems.get(fileId)

  /** *
    * Finds FileSystemItem by fileid
    *
    * @param fileId
    * @return
    */
  def findFileByItem(fileId: Int): Option[FileSystemItem] = Option(discoveredFSItems.getOrElse(fileId, null))

}
