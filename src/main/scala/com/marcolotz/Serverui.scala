package com.marcolotz

import com.marcolotz.configuration.ConfigurationManager

object Serverui extends App {

  // TODO: Command line arguments for diffent
  // TODO: Find a more elegant way to output messages
  println("Loading configuration")
  val config = new ConfigurationManager().load()

  // Launch server
  println("Loading server")
  //val server = new ContentServer

  println("Hello, serverui")
}
