import java.io.{File, FileInputStream, OutputStream, RandomAccessFile}
import java.nio.channels.{Channels, FileChannel}
import java.util.Date
import javax.servlet.http.HttpServletResponse

import com.marcolotz.auth.AuthenticationSupport
import com.marcolotz.filesystem.{FileSystemItem, FileSystemManager}
import com.typesafe.scalalogging.LazyLogging
import org.eclipse.jetty.http.HttpHeaders
import org.scalatra.{NotFound, Ok, ScalatraServlet}


/** *
  * Stream servlet. FileId are used instead of paths, in order to
  * prevent the user from having server-side filesystem information.
  */
class StreamServlet extends ScalatraServlet with AuthenticationSupport with LazyLogging {

  get("/:fileId") {

    FileSystemManager.getFileByItemId(util.Try(params("id").toInt).getOrElse(0)) match {
      case Some(requestedFile) => {
        if (requestedFile.isPlayable) {
          val range = Option[String](request.getHeader("Range"))
          logger.debug("Range: " + request.getHeader("Range"))
          streamMedia(new File(requestedFile.absolutePath), range, response)
        }
        else
          {
            logger.debug("Requested item is not watchable")
            NotFound("Requested item is not watchable")
          }
      }
      case None => {
        logger.debug("watchable file item id could not be found in the reported items")
        NotFound("Sorry, the file could not be found")
      }
    }
  }

  private def streamMedia(file: File, range: Option[String],
                  response: HttpServletResponse) =
  {
    val mediaType = ResponseHandler.resolveContentType(file.getAbsolutePath)

    response.setContentType(mediaType)

    // Firefox, Opera and IE do not send range headers
    if (range.isEmpty){
      response.setStatus(200)
      response.setHeader(HttpHeaders.CONTENT_LENGTH, file.length().toString)
      //Ok()

    }
    else
      {
        val responseRange = getResponseRange(file, range.get)
        response.setStatus(206)
        response.setHeader(HttpHeaders.ACCEPT_RANGES, "bytes")
        response.setHeader(HttpHeaders.CONTENT_RANGE, responseRange)
        response.setHeader(HttpHeaders.CONTENT_LENGTH, streamer.getLength)
        response.setHeader(HttpHeaders.LAST_MODIFIED, new Date(file.lastModified()).toString)
        //Ok()
      }
  }

  private def getResponseRange(file: File, range: String): String = {

    logger.debug("responde range: " + range)
    val ranges = range.split("=")(1).split("-")

    val from: Int = Integer.parseInt(ranges(0));

    // 1MB Chunk size
    val CHUNK_SIZE: Int = 1024 * 1024

    /**
      * Chunk media if the range upper bound is unspecified. Chrome sends "bytes=0-"
      */
    var to = from + CHUNK_SIZE

    if (to >= file.length()) {
      to = (file.length() - 1).toInt
    }

    // request already informed the expected size of the chunk
    if (ranges.length == 2) {
      to = Integer.parseInt(ranges(1));
    }

    String.format("bytes %d-%d/%d", from, to, file.length());
  }

  private def getStreamingOutput(file: File): StreamingOutput =
  {
    new StreamingOutput(){
      @Override
      def write(output: OutputStream){
        val outputChannel = Channels.newChannel(output)
        val inputChannel = new FileInputStream(file).getChannel()
          inputChannel.transferTo(0, inputChannel.size(), outputChannel);
      }
    }
  }

  private class FileStreamer extends StreamingOutput {

    var length: Int;
    var raf: RandomAccessFile;
    var buffer: Byte[]  = new Byte[4096];

    public MediaStreamer(int length, RandomAccessFile raf) {
      this.length = length;
      this.raf = raf;
    }

    @Override
    public void write(OutputStream outputStream) throws IOException, WebApplicationException {
      try {
        while( length != 0) {
          int read = raf.read(buf, 0, buf.length > length ? length : buf.length);
          outputStream.write(buf, 0, read);
          length -= read;
        }
      } finally {
        raf.close();
      }
    }

    public int getLenth() {
      return length;
    }
  }

}
