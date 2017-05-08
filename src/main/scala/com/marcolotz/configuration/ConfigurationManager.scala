package com.marcolotz.configuration

import java.io.FileNotFoundException
import java.nio.file.{Files, Paths}

import com.typesafe.scalalogging.LazyLogging
import org.json4s._
import org.json4s.jackson.Serialization.read

/**
  * Created by prometheus on 19/04/2017.
  */
object ConfigurationManager extends LazyLogging {

  private var serverConfiguration: ServerConfiguration = null

  /** *
    * Load configuration file from path
    *
    * @param path
    */
  def load(path: String = "conf/config.json"): Unit = {
    implicit val formats = DefaultFormats

    val jsonString = new String(Files.readAllBytes(Paths.get(path)))
    logger.debug(jsonString)
    Option(read[ServerConfiguration](jsonString)) match {
      case Some(conf) => serverConfiguration = conf
      case None => {
        logger.error("configuration file not found at " + path)
        throw new FileNotFoundException(path)
      }
    }
  }

  /** *
    * Return the configuration parameters
    *
    * @return
    */
  def getConguration(): ServerConfiguration = {
    serverConfiguration
  }

}
