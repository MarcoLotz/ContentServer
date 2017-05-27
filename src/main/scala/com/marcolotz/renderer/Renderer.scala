package com.marcolotz.renderer

import com.marcolotz.filesystem.{FileSystemItem, FileSystemManager}
import org.fusesource.scalate._

/** *
  * Renderer object for HTML web-pages.
  *
  * @author Marco Lotz
  */
object Renderer {

  /** *
    * Rendering engine
    */
  val engine = new TemplateEngine

  /** *
    * Content server HTML page render
    *
    * @param files
    * @param topDirectory
    * @return
    */
  def renderContentServer(files: List[FileSystemItem], topDirectory: FileSystemItem)
  : String = {
    def populateContentItemTemplate(file: FileSystemItem): String = {
      val itemMap = Map("item" -> file)
      engine.layout(file.getHtmlTemplatePath(), itemMap)
    }

    val fileInfo: List[String] = files.map(item => populateContentItemTemplate(item))
    val filesystemInfo = fileInfo.mkString(" ")

    engine.layout("templates/contentServer.jade",
      Map("innerContent" -> filesystemInfo, "topDirectory" ->
        FileSystemManager.generateRelativePathFromRoot(topDirectory.absolutePath)))
  }

  /** *
    * Landing page HTML render
    *
    * @return
    */
  def renderLandingServer(): String = {
    engine.layout("templates/landingServer.jade")
  }

  /** *
    * Renders a HTML page with the playable file
    *
    * @param playableFile
    * @return
    */
  def renderPlayableFile(playableFile: FileSystemItem): String = {
    engine.layout("templates/streamVideo.jade", Map("item" -> playableFile))
  }

}
