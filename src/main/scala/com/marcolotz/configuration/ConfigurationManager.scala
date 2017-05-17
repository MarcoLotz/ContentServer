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
  val parser = new scopt.OptionParser[Unit]("sbt run") {
    head("Content Server", "1.0")

    opt[String]('m', "mount").optional.foreach(x => c = c.copy(mountPath = x)).
      text("file system absolute mounting path")

    opt[String]('t', "tmpdir").optional.foreach(x => c = c.copy(tempDirectory = x)).
      text("temporary folder used to store downloaded compressed directory")

    opt[Int]('p', "port").optional.foreach(x => c = c.copy(port = x)).
      validate(x =>
        if ((x > 1) && (x < 65535)) success
        else failure("Value must be between 1 and 65535")).
      text("web server port")

    opt[Unit]('a', "authentication").optional.
      foreach(_ => c = c.copy(enableUserAuthentication = true)).
      text("enable user authentication")

    opt[String]('u', "username").optional.foreach(x => c = c.copy(username = x)).
      text("username expected on authentication")

    opt[String]('p', "password").optional.foreach(x => c = c.copy(password = x)).
      text("password for authentication")

    opt[Unit]('i', "hiddenFiles").optional.
      foreach(x => c = c.copy(showHiddenFiles = true)).
      text("list hidden files during file system exploration")

    // scalastyle:off
    opt[String]('e', "filtered-extensions").optional().foreach(x =>
      c = c.copy(filteredoutExtensions = x.replace("\"", "").split(" +").toList)).
      text("list of extensions to be ignored, use format \"extension1 extension2\"")
    // scalastyle:on

    help("help").text("prints this usage text")
  }
  private var c: ServerConfiguration = null

  /** *
    * Load configuration file from path
    *
    * @param path
    * @throws IllegalArgumentException
    */
  @throws[IllegalArgumentException]
  def load(path: String = "conf/config.json", args: Array[String] = Array()): Unit = {
    implicit val formats = DefaultFormats

    // load configuration file
    val jsonString = new String(Files.readAllBytes(Paths.get(path)))
    logger.debug(jsonString)

    Option(read[ServerConfiguration](jsonString)) match {
      case Some(conf) => {
        c = conf
        updateConfigFromArgs(args)
      }
      case None => {
        logger.error("configuration file not found at " + path)
        throw new FileNotFoundException(path)
      }
    }
  }

  /** *
    * overwrites the loaded configuration file with the command line
    * arguments. Note: THe application requires a conf file all the time.
    *
    * @param args
    * @throws IllegalArgumentException
    * @return
    */
  @throws[IllegalArgumentException]
  private def updateConfigFromArgs(args: Array[String])
  : Unit = {
    if (!parser.parse(args)) {
      // arguments are bad, usage message will have to be displayed
      throw new IllegalArgumentException()
    }
  }

  /** *
    * Return the configuration parameters
    *
    * @return
    */
  def getConguration(): ServerConfiguration = {
    c
  }

}
