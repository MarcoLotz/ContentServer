package com.marcolotz.filesystem

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.{FileUtils, FilenameUtils}

/**
  * Created by prometheus on 26/04/2017.
  */
object FileSystemItemFactory {

  abstract class FileSystemItemImp(file: File) extends LazyLogging with FileSystemItem
  {
    override val name = file.getName
    override val isReadable = file.canRead
    override val isDirectory = file.isDirectory
    override val isFile = !isDirectory
    override val extension = ""

    logger.debug("item: " + name + "\n\tsize:" + size)
  }

  private class FileSystemDirectory(file: File) extends FileSystemItemImp (file){

    override val size = FileUtils.sizeOfDirectory(file)
    override val humanReadableSize = FileUtils.byteCountToDisplaySize(size)
    override def getHtmlTemplatePath(): String = {
      "templates/contentDirectory.jade"
    }
  }

  private class FileSystemFile (file: File) extends FileSystemItemImp (file) {
    override val size = file.length()
    override val humanReadableSize = FileUtils.byteCountToDisplaySize(size)
    override val extension: String = FilenameUtils.getExtension(file.getAbsolutePath)
    override def getHtmlTemplatePath(): String = {
        "templates/contentFile.jade"
    }
  }

  def apply(f: File): FileSystemItem =
  {
    if (f.isDirectory) new FileSystemDirectory(f)
    else new FileSystemFile(f)
  }
}
