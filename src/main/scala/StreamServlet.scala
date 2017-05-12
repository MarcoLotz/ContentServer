import com.marcolotz.auth.AuthenticationSupport
import com.marcolotz.filesystem.FileSystemManager
import com.marcolotz.renderer.Renderer
import com.typesafe.scalalogging.LazyLogging
import org.scalatra.ScalatraServlet


/** *
  * Stream servlet. FileId are used instead of paths, in order to
  * prevent the user from having server-side filesystem information.
  */
class StreamServlet extends ScalatraServlet with AuthenticationSupport with LazyLogging {

  before() {
    // TODO: What content type??
    contentType = "text/html"
  }

  /** *
    * Serve stremaing page for given file
    */
  get("/") {
    val fileId = params.getOrElse("fileId", {
      // there's no such resource
      logger.debug("empty value for the parameter fileId.")
      halt(404)
    }).toInt

    val playableFile = FileSystemManager.getFileByItemId(fileId).getOrElse({
      logger.debug("file system item id could not be found in the reported items")
      halt(404)
    })

    if (!playableFile.isPlayable){
      logger.debug("file requested is not a playable file")
      halt(404)
    }

    Renderer.renderPlayableFile(playableFile)
  }
}
