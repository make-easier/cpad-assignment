//package com.utils
//
//import java.io.File
//import java.time.LocalDateTime
//import scala.collection.mutable.{Map => MMap}
//import scala.util.Failure
//import scala.util.Success
//import scala.util.Try
//import org.joda.time.DateTime
//import com.amazonaws.services.s3.model.{Bucket, BucketLifecycleConfiguration}
//
//import javax.inject.Inject
//import javax.inject.Singleton
//import play.api.Configuration
//import play.api.Logger
//
//import scala.collection.mutable
//import scala.concurrent.Future
//
//case class MediaFile(fileName: String, file: File, mediaType: Option[String] = None)
//
///**
// * Created by S Suryakant on 05/08/21.
// */
//@Singleton
//class S3Manager @Inject() (playConfig: Configuration,
//                           dateUtils: DateUtils,
//                           concurrencyUtils: ConcurrencyUtils) {
//
//  import concurrencyUtils.blockingPool
//  import concurrencyUtils.ec
//
//  private val logger: Logger = Logger(getClass)
//
//  private val awsConfig = playConfig.getConfig("loscars.aws").get
//
//  private val s3MoviesBucket = awsConfig.getString("MOVIES_BUCKET").get
//
//
//  private var s3DefaultBucket: Bucket = new Bucket
//  private var s3SavedBucket: Bucket = new Bucket
//  private val awsSaveBucket: String = s3MoviesBucket
//
//  private val primaryRegionStr = awsConfig.getString("AWS_S3_REGION").get
//  private val primaryRegion = S3Regions.getRegion(primaryRegionStr)
//  private val secondaryRegionStr = awsConfig.getString("AWS_S3_REGION_SECONDARY").get
//  private val secondaryRegion = S3Regions.getRegion(secondaryRegionStr)
//  Logger.info(s"primary region is: $primaryRegion, secondary region is: $secondaryRegion")
//  private val retryCount = awsConfig.getInt("AWS_MAX_UPLOAD_RETRY_COUNT").getOrElse(3)
//  private val secondaryRegionBucketSuffix = awsConfig.getString("SECONDARY_REGION_BUCKET_SUFFIX").getOrElse("backup")
//
//  private val primaryCache: MMap[String, Bucket] = MMap.empty[String, Bucket]
//  private val secondaryCache: MMap[String, Bucket] = MMap.empty[String, Bucket]
//
//
//  private val primaryS3Client: S3Client = S3Client(primaryRegion, retryCount, blockingPool)
//  private val secondaryS3Client: S3Client = S3Client(secondaryRegion, retryCount, blockingPool)
//
//  Try {
//    s3SavedBucket = primaryS3Client.getOrCreateBucket(awsSaveBucket).get
//    s3SavedBucket.setName(awsSaveBucket)
//  } match {
//    case Success(bf) =>
//      logger.info(s"S3 Bucket set to: ${s3DefaultBucket.getName}")
//      logger.info(s"S3 Saved Bucket set to: ${s3SavedBucket.getName}")
//    case Failure(th) =>
//      th match {
//        case ase: Exception =>
//          logger.error("Error while initializing S3 buckets" + ase.getMessage)
//      }
//  }
//
//  def getPrimaryBucket(bucket: String): Bucket = {
//    primaryCache.getOrElseUpdate(bucket, primaryS3Client.getOrCreateBucket(bucket).get)
//  }
//
//  private def getSecondaryBucket(bucket: String): Bucket = {
//    secondaryCache.getOrElseUpdate(bucket, secondaryS3Client.getOrCreateBucket(bucket).get)
//  }
//
//  def getPrimaryBucket(bucket: String, bucketConfiguration: BucketLifecycleConfiguration): Option[Bucket] = {
//    primaryCache.get(bucket) match {
//      case Some(v) => Option(v)
//      case None =>
//        primaryS3Client.getOrCreateBucket(bucket, bucketConfiguration).map { newBucket =>
//          primaryCache(bucket) = newBucket
//          newBucket
//        }
//    }
//  }
//
//  private def getSecondaryBucket(bucket: String, bucketConfiguration: BucketLifecycleConfiguration): Option[Bucket] = {
//    secondaryCache.get(bucket) match {
//      case Some(v) => Option(v)
//      case None =>
//        secondaryS3Client.getOrCreateBucket(bucket, bucketConfiguration).map{ newBucket =>
//          secondaryCache(bucket) = newBucket
//          newBucket
//        }
//    }
//  }
//
//  def deleteFilesFromBucket(bucketName: String, fileNames: String*): Future[Boolean] = {
//    primaryS3Client.deleteObjects(bucketName, fileNames: _*)
//  }
//
//  def getFileUrl(bucket: String, objectKey: String, isSaved: Boolean, expireInMinutes: Int = 60): Future[String] = {
//    val bucketName = if (isSaved) {
//      s3SavedBucket.getName
//    } else if (bucket != null && bucket.nonEmpty) {
//      bucket
//    } else {
//      s3DefaultBucket.getName
//    }
//    val expiryDate = LocalDateTime.now().plusMinutes(expireInMinutes)
//    Future(primaryS3Client.getPresignedURL(bucketName, objectKey, dateUtils.asDate(expiryDate)).getOrElse(""))
//  }
//
//  def getPresignedURL(objectKey: String, bucket: String = s3MoviesBucket, expireInMinutes: Int = 60): Future[Option[String]] = {
//    val expiryDate = LocalDateTime.now().plusMinutes(expireInMinutes)
//    Future(primaryS3Client.getPresignedURL(bucket, objectKey, dateUtils.asDate(expiryDate)))
//  }
//
//  def saveMedia(bucketName: String, mediaFileNames: Array[String]): Unit = {
//    primaryS3Client.changeMediaBucket(bucketName, s3SavedBucket.getName, mediaFileNames)
//  }
//
//  def moveMediaFiles(mediaFileNames: Array[String], toBucket: String): Unit = {
//    val fromBucket = s3SavedBucket.getName
//    primaryS3Client.moveFiles(fromBucket, toBucket, mediaFileNames)
//  }
//
//  def unsaveMedia(mediaFileNames: Array[String]): Unit = {
//    primaryS3Client.deleteObjects(s3SavedBucket.getName, mediaFileNames:_*)
//  }
//
//  private def uploadFile(s3Client: S3Client, bucket: Bucket, mediaFile: MediaFile): Future[Boolean] = {
//    Logger.debug(s"uploading ${mediaFile.fileName} to bucket ${bucket.getName} in region ${s3Client.getRegion}")
//    val upload = s3Client.uploadFileToBucket(bucket.getName, mediaFile)
//    upload onComplete {
//      case Success(v) =>
//        if (v) Logger.info(s"uploaded file ${mediaFile.fileName} to bucket ${bucket.getName} in region ${s3Client.getRegion}")
//        else Logger.warn(s"uploading file ${mediaFile.fileName} to bucket ${bucket.getName} in region ${s3Client.getRegion} failed")
//      case Failure(th) =>
//        Logger.error(s"error while uploading file ${mediaFile.fileName} to bucket ${bucket.getName} in region ${s3Client.getRegion}")
//        th.printStackTrace()
//    }
//    upload
//  }
//
//  def uploadFileToBucket(filename: String, file: File, bucketStr: String, mediaType: Option[String] = None): Future[Boolean] = {
//    val primaryBucket = getPrimaryBucket(bucketStr)
//    val mediaFile = MediaFile(filename, file, mediaType)
//    uploadFile(primaryS3Client, primaryBucket, mediaFile).flatMap {
//      case false =>
//        val secondaryBucket = getSecondaryBucket(s"$bucketStr$secondaryRegionBucketSuffix")
//        Logger.info(s"uploading file $filename to ${secondaryBucket.getName} with media-type $mediaType in backup region ${secondaryS3Client.getRegion}")
//        uploadFile(secondaryS3Client, secondaryBucket, mediaFile)
//      case true => Future.successful(true)
//    }
//  }
//
////  def uploadFileToBucket(mediaFile: MediaFile, bucketConfiguration: BucketLifecycleConfiguration, bucketName: String = s3MoviesBucket): Future[Boolean] = {
////    getPrimaryBucket(bucketName, bucketConfiguration).map { primaryBucket =>
////
////      uploadFile(primaryS3Client, primaryBucket, mediaFile).flatMap {
////        case false =>
////          getSecondaryBucket(s"$bucketName$secondaryRegionBucketSuffix", bucketConfiguration).map { secondaryBucket =>
////            Logger.info(s"uploading file ${mediaFile.fileName} to bucket ${secondaryBucket.getName} with mediatype ${mediaFile.mediaType} " +
////              s"in backup region ${secondaryS3Client.getRegion}")
////            uploadFile(secondaryS3Client, secondaryBucket, mediaFile)
////          }.getOrElse(Future.successful(false))
////        case true => Future.successful(true)
////      }
////    }.getOrElse(Future.successful(false))
////  }
//
//  def getAllFiles(bucketName: String): Future[Seq[String]] = {
//    val fileUrls = primaryS3Client.getFilesKey(bucketName).map { key =>
//      getPresignedURL(key, bucketName, 120)
//    }
//    Future.sequence(fileUrls).map(Os => Os.flatten)
//  }
//}
