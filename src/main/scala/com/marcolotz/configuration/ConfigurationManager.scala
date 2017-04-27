package com.marcolotz.configuration

import com.typesafe.scalalogging.LazyLogging
import java.nio.file.{Files, Paths}
import org.json4s._
import org.json4s.jackson.Serialization.read

/**
  * Created by prometheus on 19/04/2017.
  */
class ConfigurationManager (path: String = "conf/config.json") extends LazyLogging {
// TODO: Change default path to current work directory

  def load(): ServerConfiguration = {
    implicit val formats = DefaultFormats

    val jsonString = new String(Files.readAllBytes(Paths.get(path)))
    logger.debug(jsonString)
    read[ServerConfiguration](jsonString)
  }

}
