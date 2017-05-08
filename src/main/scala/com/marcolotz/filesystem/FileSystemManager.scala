package com.marcolotz.filesystem

import java.io.{BufferedInputStream, File, FileInputStream, FileOutputStream}
import java.nio.file.NotDirectoryException
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.marcolotz.configuration.{ConfigurationManager, ServerConfiguration}
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
  /** *
    * File at the mount entry point
    */
  var rootFile: FileSystemItem = null
  /** *
    * Enables lambda to filter or not hidden files (files starting with '.')
    */
  var showHiddenFiles = false

  // Options from the configuration file
  /** *
    * Filters out a given type of extension
    */
  var filteredoutExtensions: List[String] = List()
  /** *
    * Pre analyses all the possible files on init, instead of requiring the user to
    * access that dir before being able to download it.
    */
  var preemptiveFileSystemExploration = true
  /** *
    * Stores all the reported items
    */
  var discoveredFSItems = Map[Int, FileSystemItem]()

  @throws[NotDirectoryException]
  def init(conf: ServerConfiguration): Unit = {
    rootPath = conf.mountPath
    showHiddenFiles = conf.showHiddenFiles
    preemptiveFileSystemExploration = conf.preemptiveFileSystemExploration
    filteredoutExtensions = conf.filteredoutExtensions.map(x => x.toLowerCase())

    val specifiedRootFile = new java.io.File(rootPath)

    if ((!specifiedRootFile.exists && !specifiedRootFile.isDirectory) ||
      !validPath(specifiedRootFile)) {
      throw new NotDirectoryException(
        "file "
          + specifiedRootFile.getAbsolutePath
          + " is not a directory")
    }
    else {
      rootFile = FileSystemItemFactory(specifiedRootFile)
    }

    if (preemptiveFileSystemExploration) discoveredFSItems = recursivelyExploreFS(rootFile)

    // create temp folder for storing compressed directories
    // TODO: Create a clean up after exit option, leave it on by default
    val tmpDir = new File(conf.tempDirectory)
    if (tmpDir.exists()) {
      tmpDir.getParentFile.mkdirs()
    }
  }

  /** *
    * Prevents relative paths on mount time. Only accepts absolute paths.
    *
    * @param item
    * @return
    */
  private def validPath(item: File): Boolean = item.isAbsolute

  /** *
    * Explores the file system recursively from top dir
    *
    * @return Map of all the files under the top directory
    */
  def recursivelyExploreFS(topDir: FileSystemItem): Map[Int, FileSystemItem] = {

    def recursiveExploreFS(dirList: List[FileSystemItem],
                           reportedItemsAcc: Map[Int, FileSystemItem]):
    Map[Int, FileSystemItem] = {
      if (dirList.isEmpty) reportedItemsAcc
      else {
        val newReportedFiles = exploreDirectory(dirList.head)
        val directoriesToExplore =
          dirList.tail ::: (newReportedFiles
            .filter(entry => entry._2.isDirectory)
            .map(entry => entry._2).toList)

        recursiveExploreFS(directoriesToExplore, List(reportedItemsAcc, newReportedFiles).
          flatten.toMap)
      }
    }

    logger.debug("File system exploration enabled.")

    recursiveExploreFS(List(topDir), Map())
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
    * generate Map with the files that were just discovered
    *
    * @param items
    * @return
    */
  private def reportItems(items: List[File]): Map[Int, FileSystemItem] = {
    val reportedMap: Map[Int, FileSystemItem] =
      items.map(item => FileSystemItemFactory(item))
        .map(item => item.hashCode() -> item).toMap

    // Apply filters
    val filteredMap = reportedMap.filter(item => applyFilteringFunctions(item._2))

    // Log the final output
    filteredMap.foreach(entry => logger.debug(
      "Item being reported: " + entry._2.name + " Path: " + entry._2.absolutePath))
    filteredMap
  }

  private def applyFilteringFunctions(item: FileSystemItem): Boolean = {
    filteringFunctions.removeExtensions(item) &&
      filteringFunctions.removeHiddenFiles(item)
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
  def findFileByItem(fileId: Int): Option[FileSystemItem] =
    Option(discoveredFSItems.getOrElse(fileId, null))

  // TODO: Check what happens when there are inner files inside
  /** *
    * Compress the directory into the tmp folder
    *
    * @param directory to be compressed
    * @return the compressed directory
    */
  def getCompressedDirectory(directory: FileSystemItem): File = {
    // TODO: Check implementation
    def recursiveListFiles(f: File): Array[File] = {
      val these = f.listFiles
      these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
    }

    def generateRelativePath(item: File): String = {
      item.getAbsolutePath.replace(directory.absolutePath, "")
    }

    val directoryFile = new File(directory.absolutePath)

    val content = recursiveListFiles(directoryFile)

    // TODO: Make this OS independent?
    val outputPath: String = ConfigurationManager.getConguration().tempDirectory +
      "/" +
      directoryFile.getName + ".zip"

    logger.debug("zipping file: " + directoryFile.getAbsolutePath)
    logger.debug("zip output dir: " + outputPath)

    val zip = new ZipOutputStream(
      new FileOutputStream(outputPath))

    // TODO: Fix for recursive directories
    content.foreach { file =>
      logger.debug("adding to zip: " + file.getAbsolutePath)
      zip.putNextEntry(new ZipEntry(generateRelativePath(file)))
      val in = new BufferedInputStream(new FileInputStream(file.getAbsolutePath))
      var b = in.read()
      while (b > -1) {
        zip.write(b)
        b = in.read()
      }
      in.close()
      zip.closeEntry()
    }

    // Write zip to tmp folder
    zip.close()

    new File(outputPath)
  }

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
    def removeHiddenFiles(item: FileSystemItem): Boolean = {
      if (!FileSystemManager.showHiddenFiles) !item.name.startsWith(".")
      else true
    }

    /** *
      * Filters out files from a fixed extension
      *
      * @return
      */
    def removeExtensions(item: FileSystemItem): Boolean = {
      if (!FileSystemManager.filteredoutExtensions.isEmpty) {
        !FileSystemManager.filteredoutExtensions.contains(item.extension.toLowerCase())
      } else true
    }
  }

}
