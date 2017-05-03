package com.marcolotz.renderer

import com.marcolotz.filesystem.FileSystemItem
import org.fusesource.scalate._

/** *
  * Renderer object for HTML web-pages.
  *
  * @author Marco Lotz
  */
object Renderer {

  val engine = new TemplateEngine

  def renderContentServer(files: List[FileSystemItem], topDirectory: FileSystemItem) = {
    def populateContentItemTemplate(file: FileSystemItem): String = {
      val itemMap = Map("item" -> file)
      engine.layout(file.getHtmlTemplatePath(), itemMap)
    }

    val fileInfo: List[String] = files.map(item => populateContentItemTemplate(item))
    val filesystemInfo = fileInfo.mkString(" ")

    // TODO: Better to print relative paths, add as a configuration?
    engine.layout("templates/contentServer.jade", Map("innerContent" -> filesystemInfo, "topDirectory" -> topDirectory.absolutePath))
  }

  def renderLandingServer(): String = {
    engine.layout("templates/landingServer.jade")
  }

}
