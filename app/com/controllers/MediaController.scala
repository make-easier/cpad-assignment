//package com.controllers
//
//import com.utils.{ConcurrencyUtils, S3Manager}
//
//import java.time.LocalDateTime
//import javax.inject.{Inject, Singleton}
//import play.api.{Configuration, Logger}
//import play.api.mvc.Action
//import play.api.mvc.Results._
//import play.api.mvc.BodyParsers.parse
//import play.mvc.BodyParser.Json
//
//import scala.concurrent.Future
//
///**
//  * Created by S Suryakant on 17/07/21.
//  */
//@Singleton
//class MediaController @Inject()(concurrencyUtils: ConcurrencyUtils,
//                                s3Manager: S3Manager,
//                                configuration: Configuration) {
//
//  import concurrencyUtils.ec
//  private val awsConfig = configuration.getConfig("loscars.aws").get
//  private val s3MoviesBucket = awsConfig.getString("MOVIES_BUCKET").get
//
//  def uploadMovie() = Action.async(parse.multipartFormData) { implicit request =>
//    request.body.files.headOption.map { file =>
//      Logger.info(s"file received in request body")
//      println(s"file received")
//      val tempFile = file.ref.file
//      s3Manager.uploadFileToBucket(file.filename, tempFile, s3MoviesBucket).map {
//        case true => Logger.info(s"file ${file.filename} uploaded successfully")
//          Ok(<response><status>Success</status><message>Your movie upload successfully</message></response>)
//        case false => Logger.warn(s"Failed to upload file")
//          InternalServerError(<response><status>Failure</status><message>Some Error Occured while media upload</message></response>)
//      }
//    }.getOrElse {
//      Logger.warn(s"Media file is missing in request body")
//      Future.successful(BadRequest(<response><status>Failure</status><message>no file is found in request</message></response>))
//    }
//  }
//
//  def getMovie(mv_id: Option[String]) = Action.async { implicit request =>
//    mv_id.filter(_.trim.nonEmpty).map { id =>
//      s3Manager.getPresignedURL(id).map{ OptUrl =>
//        Ok(<response><status>Success</status><fileUrl>{OptUrl.getOrElse("")}</fileUrl></response>)
//      }
//    }.getOrElse {
//      Logger.info(s"No file id is sent, so returning all files")
//      s3Manager.getAllFiles(s3MoviesBucket).map{ urls =>
//        Logger.debug(s"All urls: ${urls.mkString(",")}")
//        Ok(<response><status>Success</status>{getMovieXml(urls)}</response>)
//      }. recover {
//        case th: Throwable => Logger.warn(s"Some error occurred while fetching all files", th)
//          InternalServerError(<response><status>Failure</status><message>Some Internal server error occurred</message></response>)
//      }
//    }
//  }
//
//  private def getMovieXml(movies: Seq[String]) = {
//    <files>
//      {movies.map(url => <fileUrl>{url}</fileUrl>)}
//    </files>
//  }
//}
