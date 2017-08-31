package com.marcolotz

import java.io.File

import com.marcolotz.contentserver.filesystem.{FileSystemItem, FileSystemItemFactory}
import com.marcolotz.filesystem.FileSystemItem
import org.scalatest.FunSuite

/**
  * Created by prometheus on 01/08/2017.
  */
abstract class ContentServerTest extends FunSuite{

  protected def getFileFromResources(path: String): FileSystemItem =
    FileSystemItemFactory(new File(getClass.getResource(path).getFile))

}
