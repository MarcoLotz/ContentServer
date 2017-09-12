package com.marcolotz.contentserver.request

import java.io.File
import java.util.Properties
import javax.servlet.http.HttpServletRequest

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FilenameUtils

import scala.io.Source

/**
  * Created by Marco Lotz on 08/06/2017.
  */
object ResponseHandler extends LazyLogging {

  // Load properties lookup file
  private val properties: Properties = new Properties()
  val reader = Source.fromURL(getClass.getResource("/extensions.properties")).bufferedReader()
  properties.load(reader)

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

  /**
    * Resolves the content type of requests sent at the endpoint without defined servlets
    *
    * @param request
    * @return content type
    */
  def resolveDefaultRequestResponse(request: HttpServletRequest): String = {
    val extension = FilenameUtils.getExtension(request.getRequestURL.toString)
    val foundType = Option(properties.get(extension)) match {
      case Some(ex) => ex.toString
      case None => "text/html"
    }
    logger.debug("resolved for request at " + request.getRequestURL + " is type: " + foundType)
    foundType
  }
}
