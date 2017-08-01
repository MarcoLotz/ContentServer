package com.marcolotz.filesystem

import java.io._
import java.nio.file.{Files, NotDirectoryException, Path, Paths}
import java.util.concurrent.{Executors, TimeUnit}
import java.util.zip.{ZipEntry, ZipOutputStream}

import com.marcolotz.configuration.{ConfigurationManager, ServerConfiguration}
import com.typesafe.scalalogging.LazyLogging

/**
  * Created by Marco Lotz on 20/04/2017.
  */
// TODO: Write unit tests for this
object FileSystemManager extends LazyLogging {

  /** *
    * Thread for updating filesystem information, every 10 seconds
    */
  val executorService = Executors.newSingleThreadScheduledExecutor()
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
    * Stores all the reported items
    */
  var discoveredFSItems = Map[Int, FileSystemItem]()

  @throws[NotDirectoryException]
  def init(conf: ServerConfiguration): Unit = {
    rootPath = conf.mountPath
    showHiddenFiles = conf.showHiddenFiles
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

    executorService.scheduleAtFixedRate(new Runnable {
      def run() = {
        discoveredFSItems = recursivelyExploreFS(rootFile)
      }
    }, 0, 10, TimeUnit.SECONDS)

    // create temp folder for storing compressed directories
    // TODO: Create a clean up after exit option, leave it on by default
    // TODO: Clean up temporary compressed files and symbolic links
    // addShutdownHook(Thread hook)
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
    * Generate relative path from original root file
    *
    * @param childPath
    * @return
    */
  def generateRelativePathFromRoot(childPath: String): String = {
    "~/" + generateRelativePath(rootPath, childPath)
  }

  /** *
    * Compress the directory into the tmp folder
    *
    * @param directory to be compressed
    * @return the compressed directory
    */
  def getCompressedDirectory(directory: FileSystemItem): Option[File] = {

    @throws[IOException]
    def zipDir(zippedDir: File, zos: ZipOutputStream): Unit = {
      zippedDir.listFiles().foreach(file => {
        if (file.isDirectory) {
          // recursively zip file
          zipDir(file, zos);
        }
        else {
          zos.putNextEntry(new ZipEntry(
            generateRelativePath(file.getAbsolutePath, directory.absolutePath)));
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
      logger.debug("compressing to file: " + outputFileName)
      val zip: ZipOutputStream = new ZipOutputStream(
        new FileOutputStream(outputFileName))

      try {
        zipDir(new File(directory.absolutePath), zip)
      }
      catch {
        case e: IOException => logger.error("directory: " + directory.absolutePath +
          " could not be compressed properly")
        case unknown => logger.error("compress error " + unknown.printStackTrace())
      }
      finally {
        zip.close()
      }
    }
    Option(new File(outputFileName))
  }

  private def generateRelativePath(parentPath: String, childPath: String): String = {
    childPath.replace(parentPath, "")
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

  /** *
    * Generates a symbolic link to the file system item, on the mounted streaming
    * directory
    *
    * @param item
    */
  def generateStreamLink(item: FileSystemItem): File = {
    val linkedFile = new File("stream-content/" + item.name)
    if (!linkedFile.exists()) {
      try {
        Files.createSymbolicLink(linkedFile.toPath,
          Paths.get(item.absolutePath));
      }
      catch {
        case e: IOException => logger.error("stream file: " + item.absolutePath +
          " could not generate symbolic link")
        case e: UnsupportedOperationException => logger.
          error("symbolic link error" + e.printStackTrace())
      }
    }
    linkedFile
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
