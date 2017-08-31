package com.marcolotz.contentserver.request

import java.io.File
import java.util.Properties

import org.apache.commons.io.FilenameUtils

import scala.io.Source

/**
  * Created by Marco Lotz on 08/06/2017.
  */
object ResponseHandler {

  private val properties: Properties = new Properties()
  Source.fromInputStream(getClass().getClassLoader().getResourceAsStream("extensions.properties"))

  /** *
    * Resolves the response header content type based on the file extension
    *
    * @param resourcePath
    * @return
    */
  def resolveContentType(resourcePath: String): String = {
    val file = new File(resourcePath)
     if (file.isDirectory) "application/zip"
     else {
     val extension = Option(properties.get(FilenameUtils.getExtension(resourcePath).toLowerCase()))
       extension match {
         case Some(ex) => ex.toString
         case None => "application/octet-stream"
       }
     }
  }
}
