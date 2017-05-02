package com.marcolotz.filesystem

import com.marcolotz.configuration.ServerConfiguration

/**
  * Created by prometheus on 20/04/2017.
  */
// TODO: Write unit tests for this
object FileSystemManager{

  var rootPath: String = ""

  var showHiddenFiles = false
  var filterExtensions = false

  val discoveredFSItems = new scala.collection.mutable.HashMap[Int, FileSystemItem]

  // Filtering functions:
  val hiddenFile = (x: FileSystemItem) => if (!showHiddenFiles) !x.name.startsWith(".") else true

  // TODO: add extension filtering
  val filteredExtension = (x: FileSystemItem) => if (filterExtensions) !x.name.startsWith(".") else true

  //val filteringFunctions = Array[(FileSystemItem) => Boolean](hiddenFile _,
  //                              filteringFunctions _)

  def init(conf : ServerConfiguration) = {
    rootPath = conf.mountPath
    showHiddenFiles = conf.showHiddenFiles
  }

  // TODO: decide the type of exception
  @throws(classOf[Exception])
  def listDir(path: String = ""):List[FileSystemItem] = {
    if (!validPath()) throw new Exception("Invalid Path")
    else {
      val fsItem = retrieveFiles(rootPath + path).filter(applyOptionalFilters)
      reportFsItem(fsItem)
      fsItem
    }
  }

  def validPath(): Boolean = {
    //TODO: Don't allow paths that try to go back or are more than a folder away
    true
  }

  @throws(classOf[Exception])
  def retrieveFiles(absPath:String) : List[FileSystemItem] =
  {
    val dir = new java.io.File(absPath)

    if (dir.exists && dir.isDirectory){
      dir.listFiles().toList.map(file => FileSystemItemFactory(file))
    }
    else
    {
      throw new Exception("file is not a directory")
    }
  }

  def applyOptionalFilters(item: FileSystemItem): Boolean =
  {
    //filteringFunctions.map(func => func(item))
    true
  }

  /***
    * Report all file system items in order to avoid providing absolute paths to the client
    * @param fsItem
    */
  def reportFsItem(fsItem: List[FileSystemItem]) = {
      fsItem.foreach(item => discoveredFSItems.put(item.hashCode(), item))
  }

  def findFileByItem(fileId: Int): Option[FileSystemItem] = {
    Option(discoveredFSItems.getOrElse(fileId, null))
  }
}
