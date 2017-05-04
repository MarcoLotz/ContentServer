package com.marcolotz.filesystem

trait FileSystemItem {
  def name: String

  def size: Long

  def isReadable: Boolean

  def isDirectory: Boolean

  def isFile: Boolean

  def humanReadableSize: String

  def extension: String

  def absolutePath: String

  def getHtmlTemplatePath(): String
}