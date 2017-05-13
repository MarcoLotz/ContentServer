package com.marcolotz.filesystem

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.{FileUtils, FilenameUtils}

/**
  * Created by prometheus on 26/04/2017.
  */
object FileSystemItemFactory {

  // TODO: Found a more elegant way to load possible video extensions
  private val videoExtensions = List("asf", "asx", "avi", "mov",
    "movie", "mpe", "mpeg", "mpg", "qt", "rv", "ogg", "mp4")

  def apply(f: File): FileSystemItem = {
    if (f.isDirectory) new FileSystemDirectory(f)
    else if (videoExtensions.contains(FilenameUtils.getExtension(f.getAbsolutePath).toLowerCase)) {
      new playableItem(f)
    }
    else new FileSystemFile(f)
  }

  abstract class FileSystemItemImp(file: File) extends LazyLogging with FileSystemItem {
    override val name = file.getName
    override val isReadable = file.canRead
    override val isDirectory = file.isDirectory
    override val isFile = !isDirectory
    override val isPlayable = false
    override val extension = ""
    override val absolutePath = file.getAbsolutePath()

    logger.debug("item: " + name + "\n\tsize:" + size)

    override def equals(that: Any): Boolean =
      that match {
        case that: FileSystemItem => hashCode() == that.hashCode()
        case _ => false
      }

    override def hashCode: Int = {
      var hashCode: Int = 1
      hashCode = 31 * hashCode + name.hashCode()
      hashCode = 31 * hashCode + absolutePath.hashCode()
      return hashCode
    }
  }

  private class FileSystemDirectory(file: File) extends FileSystemItemImp(file) {

    override val size = FileUtils.sizeOfDirectory(file)
    override val humanReadableSize = FileUtils.byteCountToDisplaySize(size)

    override def getHtmlTemplatePath(): String = {
      "templates/contentDirectory.jade"
    }
  }

  private class FileSystemFile(file: File) extends FileSystemItemImp(file) {
    override val size = file.length()
    override val humanReadableSize = FileUtils.byteCountToDisplaySize(size)
    override val extension: String = FilenameUtils.getExtension(file.getAbsolutePath).toLowerCase

    override def getHtmlTemplatePath(): String = {
      "templates/contentFile.jade"
    }
  }

  private class playableItem(file: File) extends FileSystemFile(file) {
    override val isPlayable = true

    override def getHtmlTemplatePath(): String = {
      "templates/contentStream.jade"
    }
  }

}
