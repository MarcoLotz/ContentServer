package com.marcolotz.filesystem

import java.io.File

import com.marcolotz.configuration.ServerConfiguration
import com.typesafe.scalalogging.LazyLogging

/**
  * Created by prometheus on 20/04/2017.
  */
// TODO: Write unit tests for this
// TODO: Make a huge refactor on this.
object FileSystemManager extends LazyLogging {

  /***
    * Mount entry point for the content server
    */
  var rootPath: String = ""

  /***
    * File at the mount entry point
    * // TODO: Is this the best way to declare?
    */
  var rootFile: FileSystemItem = null

  // Options from the configuration files
  /** *
    * Enables lambda to filter or not hidden files (files starting with '.')
    */
  var showHiddenFiles = false
  /** *
    * Filters out a given type of extension
    */
  var filterExtensions = false

  /** *
    * Pre analyses all the possible files on init, instead of requiring the user to
    * access that dir before being able to download it.
    */
  var preemptiveFileSystemExploration = true

  /** **
    * Stores all the reported items
    */
  val discoveredFSItems = new scala.collection.mutable.HashMap[Int, FileSystemItem]

  // Filtering functions:
  val hiddenFile = (x: FileSystemItem) => if (!showHiddenFiles) !x.name.startsWith(".") else true

  // TODO: add extension filtering
  val filteredExtension = (x: FileSystemItem) => if (filterExtensions) !x.name.startsWith(".") else true

  //val filteringFunctions = Array[(FileSystemItem) => Boolean](hiddenFile _,
  //                              filteringFunctions _)

  // TODO: Throw exception if it is not a file?
  def init(conf: ServerConfiguration) = {
    rootPath = conf.mountPath
    showHiddenFiles = conf.showHiddenFiles

    val specifiedRootFile = new java.io.File(rootPath)

    if (!specifiedRootFile.exists && !specifiedRootFile.isDirectory)
    {
      // TODO: Throw exception.
      // TODO: Change this for a match statement
    }
    else
      {
        rootFile = FileSystemItemFactory(specifiedRootFile)
      }

    if (preemptiveFileSystemExploration) preexploreFileSystem()
  }

  // TODO: decide the type of exception
  // TODO: Remove in order to use @exploreFileSystem method
  //@throws(classOf[Exception])
  def listRootPath(): List[FileSystemItem] = {
    //if (!validPath()) throw new Exception("Invalid Path")
    //else {
      val fsItem = retrieveFiles(rootPath).filter(applyOptionalFilters)
      reportFsItem(fsItem)
      fsItem
    //}
  }

  def validPath(): Boolean = {
    //TODO: Don't allow paths that try to go back or are more than a folder away
    true
  }

  def validPath(item: FileSystemItem): Boolean = {
    //TODO: Don't allow paths that try to go back or are more than a folder away
    true
  }

  @throws(classOf[Exception])
  def retrieveFiles(absPath: String): List[FileSystemItem] = {
    val dir = new java.io.File(absPath)

    if (dir.exists && dir.isDirectory) {
      dir.listFiles().toList.map(file => FileSystemItemFactory(file))
    }
    else {
      throw new Exception("file is not a directory")
    }
  }

  def applyOptionalFilters(item: FileSystemItem): Boolean = {
    //filteringFunctions.map(func => func(item))
    true
  }

  /** *
    * Report all file system items in order to avoid providing absolute paths to the client
    *
    * @param fsItem
    */
  def reportFsItem(fsItem: List[FileSystemItem]) = {
    fsItem.foreach(item => {
      discoveredFSItems.put(item.hashCode(), item)
      logger.debug("Item reported! hashcode: " + item.hashCode() + " file name: " + item.name)
    })
  }

  def findFileByItem(fileId: Int): Option[FileSystemItem] = {
    Option(discoveredFSItems.getOrElse(fileId, null))
  }

  // TODO: Change to use immutable map on recursive call
  // TODO: Throw exception?
  def preexploreFileSystem() =
  {
    logger.debug("File system exploration enabled.")

    def reportItem(item: File): Unit = {
      val fsItem = FileSystemItemFactory(item)
      logger.debug("Item being reported: " + fsItem.name + " Path: " + fsItem.absolutePath)
      if (validPath(fsItem)) {
        discoveredFSItems.put(fsItem.hashCode(), fsItem)
      }
      else {
        logger.debug("Item outside expected path")
      }
    }

    def dirsToExplore(directoryList: List[File]): Unit = {

      if (!directoryList.isEmpty) {
        val exploredDir = directoryList.head
        // report all the files in that dir
        exploredDir.listFiles().toList.foreach(reportItem)
        // explore the next directories under itß
        val reportDirectories = exploredDir.listFiles().filter(file => file.isDirectory)

        dirsToExplore(List.concat(directoryList.tail, reportDirectories))
      }
    }

    val rootDir = new java.io.File(rootPath)
    if (rootDir.exists() && rootDir.isDirectory) {
      dirsToExplore(List(rootDir))
    }
  }

  def exploreFileSystemItem(file : FileSystemItem): List[FileSystemItem] = {
    val fsItems = retrieveFiles(file.absolutePath).filter(applyOptionalFilters)
    // TODO: this probably can be sent to inside the method
    if (!preemptiveFileSystemExploration) {
      reportFsItem(fsItems)
    }
    fsItems
  }

  // TODO: Throw exception?
  def exploreFileSystemItem(fileId: Int): List[FileSystemItem] =
  {
    val fsItem = discoveredFSItems.get(fileId).get

    // TODO: Add this match statement for throwing exception on invalid items
    /*fsItem match {
    case FileSystemItem(item) => exploreFileSystemItem(item)
    case None => // TODO: Throw exception
    }*/
    exploreFileSystemItem(fsItem)
  }
}
