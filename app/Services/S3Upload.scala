package Services

import java.io.File
import java.util.UUID

import akka.util.ByteString
import play.api.libs.Files.TemporaryFile
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{MultipartFormData, Request}
import play.core.parsers.Multipart.FileInfo
import utils.S3Bucket

/**
  * Created by jaideep on 01/06/17.
  */
object S3Upload {

  type FilePartHandler[B] = FileInfo => Accumulator[ByteString, FilePart[B]]

  /**
    * Uses a custom FilePartHandler to return a type of "File" rather than
    * using Play's TemporaryFile class.  Deletion must happen explicitly on
    * completion, rather than TemporaryFile (which uses finalization to
    * delete temporary files).
    *
    * @return
    */


  def uploadFile(request: Request[MultipartFormData[TemporaryFile]]): String = {

    request.body.file("name").map { file =>
      val filename = file.filename
      val contentType = file.contentType
      val newFile = new File(s"/tmp/${UUID.randomUUID}_$filename")
      file.ref.moveTo(newFile, true)

      try {
        val bucket = S3Bucket.getBucketByName("jdkracs").getOrElse(S3Bucket.createBucket("jdkracs"))
        val result = S3Bucket.createObject(bucket, filename, newFile)
        s"File uploaded with key ${result.key}"
       } catch {
          case t: Throwable => t.getMessage
       }
    }.getOrElse{
      "No body in the file"
    }
  }
}

