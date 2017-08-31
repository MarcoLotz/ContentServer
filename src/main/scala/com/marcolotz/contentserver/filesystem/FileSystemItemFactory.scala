package com.marcolotz.contentserver.filesystem

import java.io.File

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.{FileUtils, FilenameUtils}

/**
  * Created by Marco Lotz on 26/04/2017.
  */
object FileSystemItemFactory extends LazyLogging{

  private val playableExtensions = List(
    // Video formats
    "mpg", "mpeg", "avi", "wmv", "mov", "rm", "ram",
    "swf", "flv", "ogg", "webm", "mp4",
    // Audio Formats
    "mid", "midi", "wma", "aac", "wav", "mp3")

  def apply(f: File): FileSystemItem = {
    if (f.isDirectory) new FileSystemDirectory(f)
    else if (playableExtensions.contains(
      FilenameUtils.getExtension(f.getAbsolutePath).toLowerCase)) {
      new PlayableItem(f)
    }
    else new FileSystemFile(f)
  }

  abstract class FileSystemItemImp(file: File) extends FileSystemItem {
    override val name = file.getName
    override val isReadable = file.canRead
    override val isDirectory = file.isDirectory
    override val isFile = !isDirectory
    override val isPlayable = false
    override val extension = ""
    override val absolutePath = file.getAbsolutePath()

    override def equals(that: Any): Boolean =
      that match {
        case that: FileSystemItem => hashCode() == that.hashCode()
        case _ => false
      }

    override def hashCode: Int = {
      var hashCode: Int = 1
      hashCode = 31 * hashCode + name.hashCode()
      hashCode = 31 * hashCode + absolutePath.hashCode()
      return Math.abs(hashCode)
    }
  }

  protected class FileSystemDirectory(file: File) extends FileSystemItemImp(file) {

    override val size = FileUtils.sizeOfDirectory(file)
    override val humanReadableSize = FileUtils.byteCountToDisplaySize(size)

    override def getHtmlTemplatePath(): String = {
      "templates/contentDirectory.jade"
    }
  }

  protected class FileSystemFile(file: File) extends FileSystemItemImp(file) {
    override val size = file.length()
    override val humanReadableSize = FileUtils.byteCountToDisplaySize(size)
    override val extension: String = FilenameUtils.getExtension(file.getAbsolutePath).toLowerCase

    override def getHtmlTemplatePath(): String = {
      "templates/contentFile.jade"
    }
  }

  protected class PlayableItem(file: File) extends FileSystemFile(file) {
    override val isPlayable = true

    override def getHtmlTemplatePath(): String = {
      "templates/contentStream.jade"
    }
  }

}
