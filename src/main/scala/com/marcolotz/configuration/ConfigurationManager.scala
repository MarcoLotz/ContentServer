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

  /** *
    * Command line parser options
    */
  // TODO: Change to mutable parsing
  val parser = new scopt.OptionParser[ServerConfiguration]("scopt") {
    head("content server", "1.x")

    opt[String]('m', "mount").optional.action((x, c) => c.copy(mountPath = x)).
      text("file system absolute mounting path")

    opt[String]('t', "tmpdir").optional.action((x, c) => c.copy(tempDirectory = x)).
      text("temporary folder used to store downloaded compressed directory")

    opt[Int]('p', "port").optional.action((x, c) => c.copy(port = x)).
      text("web server port")

    opt[Unit]('a', "authentication").optional.
      action((_, c) => c.copy(enableUserAuthentication = true)).
      text("enable user authentication")

    opt[String]('u', "username").optional.action((x, c) => c.copy(username = x)).
      text("username expected on authentication")

    opt[String]('p', "password").optional.action((x, c) => c.copy(password = x)).
      text("password for authentication")

    opt[Unit]('i', "hiddenFiles").optional.action((_, c) => c.copy(showHiddenFiles = true)).
      text("list hidden files during file system exploration")

    // TODO: Implement list of extensions
    // opt[String]('e',"filtered-extensions").unbounded().optional().action( (x, c) =>
    //  c.copy(files = c.files :+ x) ).text("optional unbounded args")

    help("help").text("prints this usage text")
  }

  private var serverConfiguration: ServerConfiguration = null

  /** *
    * Load configuration file from path
    *
    * @param path
    */
  def load(path: String = "conf/config.json", args: Array[String] = Array()): Unit = {
    implicit val formats = DefaultFormats
    var storedJsonConfiguration: ServerConfiguration = null

    val jsonString = new String(Files.readAllBytes(Paths.get(path)))
    logger.debug(jsonString)

    Option(read[ServerConfiguration](jsonString)) match {
      case Some(conf) => storedJsonConfiguration = conf
      case None => {
        logger.error("configuration file not found at " + path)
        throw new FileNotFoundException(path)
      }
    }

    serverConfiguration = updateConfigFromArgs(args, storedJsonConfiguration)
  }

  private def updateConfigFromArgs(args: Array[String], jsonStoredConfig: ServerConfiguration)
  : ServerConfiguration = {
    parser.parse(args, ServerConfiguration()) match {
      case Some(config) =>
        compareWithJson(config, jsonStoredConfig)
      case None =>
        // TODO: Define here
        logger.debug("parameters error")
        jsonStoredConfig
      // arguments are bad, error message will have been displayed
    }
  }

  private def compareWithJson(parsedConfig: ServerConfiguration,
                              jsonStoredConfig: ServerConfiguration):
  ServerConfiguration = {
    // TODO overwrite when it has non-default values
    jsonStoredConfig
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
