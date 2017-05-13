import java.io.FileInputStream

import com.marcolotz.auth.AuthenticationSupport
import com.marcolotz.filesystem.{FileSystemItem, FileSystemManager}
import com.marcolotz.renderer.Renderer
import com.typesafe.scalalogging.LazyLogging
import org.scalatra.ScalatraServlet


/** *
  * Stream servlet. FileId are used instead of paths, in order to
  * prevent the user from having server-side filesystem information.
  */
class StreamServlet extends ScalatraServlet with AuthenticationSupport with LazyLogging {

  val tmpStreamDirectory = "/stream/"

  before() {
    // TODO: What content type??
    contentType = "text/html"
  }

  /** *
    * Copy file to local stream directory. If already copied does nothing
    *
    * @param file
    */
  private def serveStreamFile(file: FileSystemItem) = {
    // TODO: Check if file already exists
    val fis: FileInputStream = new FileInputStream(file.absolutePath)

    // TODO: Find a more elegant way to write the file
    var b: Int = 0;
    while ((b = fis.read()) != -1) {
      response.getOutputStream().write(b);
    }
  }

  get("/:fileId") {
    val fileId = params.getOrElse("fileId", {
      // there's no such resource
      logger.debug("empty value for the parameter fileId.")
      halt(404)
    }).toInt

    val playableFile = FileSystemManager.getFileByItemId(fileId).getOrElse({
      logger.debug("file system item id could not be found in the reported items")
      halt(404)
    })

    if (!playableFile.isPlayable) {
      logger.debug("file requested is not a playable file")
      halt(404)
    }

    // TODO: Found a more elegant way than data duplication
    serveStreamFile(playableFile)
    Renderer.renderPlayableFile(playableFile)
  }
}
