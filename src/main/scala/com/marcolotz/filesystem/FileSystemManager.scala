package com.marcolotz.filesystem

import java.io._
import java.nio.file.{Files, NotDirectoryException, Path, Paths}
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

    // TODO: Still required to not have preemptive file system exploration?
    if (preemptiveFileSystemExploration) discoveredFSItems = recursivelyExploreFS(rootFile)

    // create temp folder for storing compressed directories
    // TODO: Create a clean up after exit option, leave it on by default
    val tmpDir = new File(conf.tempDirectory)
    if (!tmpDir.exists()) {
      tmpDir.mkdirs()
    }
  }

  /** *
    * Lists the content of a directory
    *
    * @param topDir
    * @return
    */
  def listDirectory(topDir: FileSystemItem): List[FileSystemItem] = {
    (new File(topDir.absolutePath)).listFiles().map(FileSystemItemFactory(_)).toList
  }

  /** *
    * Explores the file system recursively from top dir
    *
    * @return Map of all the files under the top directory
    */
  def recursivelyExploreFS(topDir: FileSystemItem): Map[Int, FileSystemItem] = {

    logger.debug("File system exploration enabled.")

    recursiveListFiles(new File(topDir.absolutePath)).
      map(FileSystemItemFactory(_)).
      filter(applyFilteringFunctions(_)).
      map(item => item.hashCode() -> item).
      toMap
  }

  /** *
    * Finds FileSystemItem by fileid
    *
    * @param fileId
    * @return
    */
  def getFileByItemId(fileId: Int): Option[FileSystemItem] =
    Option(discoveredFSItems.getOrElse(fileId, null))

  /** *
    * Compress the directory into the tmp folder
    *
    * @param directory to be compressed
    * @return the compressed directory
    */
  // TODO: What if there is an error in the file compressions?
  def getCompressedDirectory(directory: FileSystemItem): Option[File] = {

    def generateRelativePath(item: File): String = {
      item.getAbsolutePath.replace(directory.absolutePath, "")
    }

    @throws[IOException]
    def zipDir(zippedDir: File, zos: ZipOutputStream): Unit = {
      zippedDir.listFiles().foreach(file => {
        if (file.isDirectory) {
          // recursively zip file
          zipDir(file, zos);
        }
        else {
          zos.putNextEntry(new ZipEntry(generateRelativePath(file)));
          val filePath: Path = Paths.get(String.valueOf(file));
          Files.copy(filePath, zos);
          zos.closeEntry()
        }
      })
    }

    val outputFileName = ConfigurationManager.getConguration().tempDirectory + File.separator +
      directory.name + ".zip"

    // If it has not been compressed before, perform compression
    if (!Files.exists(Paths.get(outputFileName))) {
      logger.info("compressing to file: " + outputFileName)
      val zip: ZipOutputStream = new ZipOutputStream(
        new FileOutputStream(outputFileName))

      try {
        zipDir(new File(directory.absolutePath), zip)
      }
      catch {
        case e: IOException => logger.error("directory: " + directory.absolutePath +
          " could not be compressed properly")
        case unkown => logger.error("compress error " + unkown.printStackTrace())
      }
      finally {
        zip.close()
      }
    }
    Option(new File(outputFileName))
  }

  /** *
    * Prevents relative paths on mount time. Only accepts absolute paths.
    *
    * @param item
    * @return
    */
  // TODO: Check if works as expected
  private def validPath(item: File): Boolean = item.isAbsolute

  /** *
    * List files recursively
    *
    * @param f base file for listing
    * @return
    */
  private def recursiveListFiles(f: File): Array[File] = {
    val these = f.listFiles
    these ++ these.filter(_.isDirectory).flatMap(recursiveListFiles)
  }

  private def applyFilteringFunctions(item: FileSystemItem): Boolean = {
    filteringFunctions.removeExtensions(item) &&
      filteringFunctions.removeHiddenFiles(item)
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
