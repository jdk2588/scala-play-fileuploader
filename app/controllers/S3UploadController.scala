package controllers

import java.io.File
import java.nio.file.{Files, Path}
import java.nio.file.attribute.PosixFilePermission.{OWNER_READ, OWNER_WRITE}
import java.nio.file.attribute.PosixFilePermissions
import java.util
import javax.inject.{Inject, Singleton}

import Services.S3Upload
import Services.S3Upload.FilePartHandler
import akka.stream.IOResult
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import play.api.i18n.MessagesApi
import play.api.libs.streams.Accumulator
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Created by jaideep on 01/06/17.
  */
@Singleton
class S3UploadController @Inject()(val reactiveMongoApi: ReactiveMongoApi, implicit val ec: ExecutionContext) extends Controller with MongoController with ReactiveMongoComponents {

  def multipartupload = Action(parse.multipartFormData) { request =>
    request.body.file("name").map { name =>
      import java.io.File
      val filename = name.filename
      val contentType = name.contentType
      name.ref.moveTo(new File(s"/tmp/picture/$filename"))
    }
    Ok("File Uploaded")
  }

  def upload = Action(parse.multipartFormData) { implicit request =>
    val result = S3Upload.uploadFile(request)
    Ok(result)
  }

  type FilePartHandler[B] = FileInfo => Accumulator[ByteString, FilePart[B]]

  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType) =>
      val attr = PosixFilePermissions.asFileAttribute(util.EnumSet.of(OWNER_READ, OWNER_WRITE))
      val path: Path = Files.createTempFile("multipartBody", "tempFile", attr)
      val file = path.toFile
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      println(s"$fileSink")

      accumulator.map {
        case IOResult(count, status) => {

          println(partName, filename, contentType, file)
          FilePart(partName, filename, contentType, file)
        }
      }
  }

  /**
    * A generic operation on the temporary file that deletes the temp file after completion.
    */
  private def operateOnTempFile(file: File) = {
    val size = Files.size(file.toPath)
    Files.deleteIfExists(file.toPath)
    size
  }

  def customupload = Action(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    val fileOption = request.body.file("name").map {
      case FilePart(key, filename, contentType, file) =>
        //        logger.info(s"key = ${key}, filename = ${filename}, contentType = ${contentType}, file = $file")
        val data = operateOnTempFile(file)
        data
    }
    Ok(s"file size = ${fileOption.getOrElse("no file")}")
  }

}
