package com.marcolotz.filesystem

/**
  * Created by prometheus on 20/04/2017.
  */
// TODO: Write unit tests for this
class FileSystemManager(rootPath: String){

  val showHiddenFiles = false
  val filterExtensions = false

  // Filtering functions:
  val hiddenFile = (x: FileSystemItem) => if (!showHiddenFiles) !x.name.startsWith(".") else true

  // TODO: add extension filtering
  val filteredExtension = (x: FileSystemItem) => if (filterExtensions) !x.name.startsWith(".") else true

  //val filteringFunctions = Array[(FileSystemItem) => Boolean](hiddenFile _,
  //                              filteringFunctions _)

  // TODO: decide the type of exception
  @throws(classOf[Exception])
  def listDir(path: String = ""):List[FileSystemItem] = {
    if (!validPath()) throw new Exception("Invalid Path")
    else retrieveFiles(rootPath + path).filter(applyOptionalFilters)
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
}
