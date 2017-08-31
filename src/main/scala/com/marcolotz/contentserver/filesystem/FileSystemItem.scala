package com.marcolotz.contentserver.filesystem

/**
  * Created by prometheus on 31/08/2017.
  */
trait FileSystemItem {
  def name: String

  def size: Long

  def isReadable: Boolean

  def isDirectory: Boolean

  def isFile: Boolean

  def isPlayable: Boolean

  def humanReadableSize: String

  def extension: String

  def absolutePath: String

  def getHtmlTemplatePath(): String
}
