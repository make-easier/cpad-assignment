//package com.utils
//
//import java.io.FileInputStream
//import java.util.Date
//
//import com.amazonaws._
//import com.amazonaws.auth.InstanceProfileCredentialsProvider
//import com.amazonaws.regions.Regions
//import com.amazonaws.services.s3.AmazonS3ClientBuilder
//import com.amazonaws.services.s3.model.DeleteObjectsRequest.KeyVersion
//import com.amazonaws.services.s3.model._
//import play.api.Logger
//
//import scala.concurrent.{ExecutionContext, Future}
//import scala.util.{Failure, Success, Try}
//
///**
// * Created by S Suryakant on 05/08/21.
// */
//class S3Client(region: Regions, retryCount: Int, implicit val ec: ExecutionContext) {
//
//  private val clientConfiguration = new ClientConfiguration()
//  clientConfiguration.setMaxErrorRetry(retryCount)
//
//  private val client = AmazonS3ClientBuilder.standard()
//    .withRegion(region)
//    .withCredentials(new InstanceProfileCredentialsProvider(false))
//    .withClientConfiguration(clientConfiguration)
//    .build()
//
//  def getRegion: Regions = region
//
//  private def createBucket(bucketName: String, configuration: Option[BucketLifecycleConfiguration] = None): Option[Bucket] = {
//    Try {
//      configuration.map { config =>
//        val bucket = client.createBucket(bucketName)
//        client.setBucketLifecycleConfiguration(bucketName, config)
//        bucket
//      }.getOrElse {
//        client.createBucket(bucketName)
//      }
//    } match {
//      case Success(bucket) =>
//        Logger.info(s"bucket $bucketName created successfully.")
//        Option(bucket)
//      case Failure(th) =>
//        Logger.error(s"error occurred while creating bucket: $bucketName", th)
//        None
//    }
//  }
//
//  def getOrCreateBucket(bucketName: String): Option[Bucket] = {
//    if (isBucketPresent(bucketName)) {
//      val bucket = new Bucket
//      bucket.setName(bucketName)
//      Option(bucket)
//    } else createBucket(bucketName)
//  }
//
//  def getOrCreateBucket(bucketName: String, config: BucketLifecycleConfiguration): Option[Bucket] = {
//    if (isBucketPresent(bucketName)) {
//      val bucket = new Bucket
//      bucket.setName(bucketName)
//      Option(bucket)
//    } else createBucket(bucketName, Option(config))
//  }
//
//  def isBucketPresent(bucketName: String): Boolean = {
//    Try {
//      client.doesBucketExistV2(bucketName)
//    } match {
//      case Success(bf) => bf
//      case Failure(th) =>
//        th match {
//          case ase: AmazonServiceException => {
//            val msg = getErrorMsg(ase)
//            Logger.error(s"error occurred while checking existence for bucket $bucketName, $msg")
//          }
//          case ace: AmazonClientException => {
//            Logger.error(s"AmazonClientException while checking existence for bucket $bucketName, ${ace.getMessage}")
//          }
//        }
//        th.printStackTrace()
//        false
//    }
//  }
//
//  def isObjectPresent(bucketName: String, objectName: String): Boolean = {
//    Try {
//      client.doesObjectExist(bucketName, objectName)
//    } match {
//      case Success(bf) => bf
//      case Failure(th) =>
//        th match {
//          case ase: AmazonServiceException =>
//            val msg = getErrorMsg(ase)
//            Logger.error(s"error occurred while checking existence for object $objectName in bucket $bucketName, ${ase.getMessage}, $msg")
//          case ace: AmazonClientException =>
//            Logger.error(s"AmazonClientException while checking existence for object $objectName in bucket $bucketName, ${ace.getMessage}")
//        }
//        th.printStackTrace()
//        false
//    }
//  }
//
//  def uploadFileToBucket(bucket: String, mediaFile: MediaFile): Future[Boolean] = {
//    Future {
//      val metaData = new ObjectMetadata()
//      metaData.setContentLength(mediaFile.file.length)
//      metaData.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION)
//      mediaFile.mediaType.foreach(media => metaData.setContentType(media))
//
//      val inputStream = new FileInputStream(mediaFile.file)
//      val objectToPut: PutObjectRequest = new PutObjectRequest(bucket, mediaFile.fileName, inputStream, metaData)
//      val result = client.putObject(objectToPut)
//      Logger.debug(s"File [${mediaFile.fileName}] uploaded to $bucket has encryption status : ${result.getSSEAlgorithm}")
//      inputStream.close()
//      Option(result.getSSEAlgorithm).exists(_.equals("AES256"))
//    } recover {
//      case ase: AmazonServiceException =>
//        val msg = getErrorMsg(ase)
//        Logger.error(s"exception while uploading file ${mediaFile.fileName} to bucket $bucket, $msg")
//        ase.printStackTrace()
//        false
//
//      case sce: SdkClientException =>
//        Logger.error(s"SdkClientException while uploading file ${mediaFile.fileName} to bucket $bucket, ${sce.getMessage}")
//        sce.printStackTrace()
//        false
//
//      case ace: AmazonClientException =>
//        Logger.error(s"AmazonClientException while uploading file ${mediaFile.fileName} to bucket $bucket, ${ace.getMessage}")
//        ace.printStackTrace()
//        false
//
//      case ex: Throwable =>
//        Logger.error(s"Error occurred while uploading file ${mediaFile.fileName} to bucket $bucket, ${ex.getMessage}")
//        ex.printStackTrace()
//        false
//    }
//  }
//
//  private def getErrorMsg(ex: AmazonServiceException): String = {
//    s"""Caught an AmazonServiceException: ${ex.getMessage},
//       |Status Code: ${ex.getStatusCode},
//       |Error Code: ${ex.getErrorCode},
//       |Error Type: ${ex.getErrorType},
//       |Request ID: ${ex.getRequestId}""".stripMargin
//  }
//
//  def changeMediaBucket(sourceBucketName: String, destBucketName: String, mediaFileNames: Array[String]): Unit = {
//    Logger.info(s"""copying ${mediaFileNames.mkString(", ")} from $sourceBucketName to $destBucketName""")
//
//    def copyMedia(mediaFile: String): Unit = {
//      Future {
//        val metadata = new ObjectMetadata
//        metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION)
//
//        val copyObjRequest: CopyObjectRequest = new CopyObjectRequest(sourceBucketName, mediaFile, destBucketName, mediaFile)
//        copyObjRequest.setCannedAccessControlList(CannedAccessControlList.Private)
//        copyObjRequest.setNewObjectMetadata(metadata)
//
//        client.copyObject(copyObjRequest)
//        Logger.info(s"copied $mediaFile from $sourceBucketName to $destBucketName")
//      } recover {
//        case ase: AmazonServiceException =>
//          val msg = getErrorMsg(ase)
//          ase.printStackTrace()
//          Logger.error(s"error occurred while copying file $mediaFile from bucket $sourceBucketName to $destBucketName, $msg")
//        case ace: AmazonClientException =>
//          Logger.error(s"error occurred while copying file $mediaFile from bucket $sourceBucketName to $destBucketName, ${ace.getMessage}")
//          ace.printStackTrace()
//      }
//    }
//
//    mediaFileNames.foreach {
//      mediaFile =>
//        Logger.info(s"copying $mediaFile from $sourceBucketName to $destBucketName")
//        copyMedia(mediaFile)
//    }
//  }
//
//  def moveFiles(sourceBucketName: String, destBucketName: String, mediaFileNames: Array[String]): Unit = {
//    val names = mediaFileNames.mkString(", ")
//    Logger.info(s"""moving event-media $names from $sourceBucketName to $destBucketName""")
//
//    def copyFile(mediaFile: String, fromBucket: String, toBucket: String): Future[Boolean] = {
//      Future {
//        val metadata = new ObjectMetadata()
//        metadata.setSSEAlgorithm(ObjectMetadata.AES_256_SERVER_SIDE_ENCRYPTION)
//        val copyObjRequest: CopyObjectRequest = new CopyObjectRequest(fromBucket, mediaFile, toBucket, mediaFile)
//        copyObjRequest.setCannedAccessControlList(CannedAccessControlList.Private)
//        copyObjRequest.setNewObjectMetadata(metadata)
//        Logger.debug(s"copying $mediaFile from $fromBucket to $toBucket")
//        client.copyObject(copyObjRequest)
//        Logger.info(s"copied $mediaFile from bucket $fromBucket to bucket $toBucket")
//        true
//      } recover {
//        case ase: AmazonServiceException =>
//          val msg = getErrorMsg(ase)
//          ase.printStackTrace()
//          Logger.error(s"error occurred while copying file $mediaFile from bucket $fromBucket to $toBucket, $msg")
//          false
//        case ace: AmazonClientException =>
//          Logger.error(s"error occurred while copying file $mediaFile from bucket $fromBucket to $toBucket, ${ace.getMessage}")
//          ace.printStackTrace()
//          false
//      }
//    }
//
//    def copyFiles(fromBucketName: String, toBucketName: String, fileNames: Seq[String]): Future[Boolean] = {
//      val result = fileNames.map(fileName => copyFile(fileName, fromBucketName, toBucketName))
//      Future.sequence(result).map { flags =>
//        flags.forall(_ == true)
//      }
//    }
//
//    copyFiles(sourceBucketName, destBucketName, mediaFileNames) flatMap { copied =>
//      if(copied) {
//        Logger.info(s"copied files $names from $sourceBucketName to $destBucketName")
//        deleteObjects(sourceBucketName, mediaFileNames: _*)
//      } else {
//        Logger.info(s"failed to copy one of files $names from $sourceBucketName to $destBucketName")
//        Future.successful(false)
//      }
//    }
//
//  }
//
//  import scala.collection.JavaConversions._
//
//  private def deleteObjects(mediaFileNames: Seq[String], bucketToBeDeleted: String): Future[Boolean] = {
//    val keys = mediaFileNames.map(key => new KeyVersion(key))
//    val names = mediaFileNames.mkString(", ")
//    Future {
//      val multiObjectDeleteRequest: DeleteObjectsRequest = new DeleteObjectsRequest(bucketToBeDeleted).withKeys(keys)
//      val delObjRes: DeleteObjectsResult = client.deleteObjects(multiObjectDeleteRequest)
//      val successfulDeletes: Int = delObjRes.getDeletedObjects.size
//      Logger.info(s"""deleted s3 files $names from bucket $bucketToBeDeleted""")
//      true
//    } recover {
//      case ase: AmazonServiceException =>
//        val msg = getErrorMsg(ase)
//        Logger.error(s"""error occurred while deleting files $names from bucket $bucketToBeDeleted, $msg""")
//        ase.printStackTrace()
//        false
//      case ace: AmazonClientException =>
//        Logger.error(s"""error occurred while deleting files $names from bucket $bucketToBeDeleted, ${ace.getMessage}""")
//        ace.printStackTrace()
//        false
//    }
//  }
//
//  private def deleteObject(bucketName: String, objKey: String): Future[Boolean] = {
//    Future {
//      client.deleteObject(new DeleteObjectRequest(bucketName, objKey))
//      Logger.info(s"deleted $objKey from bucket $bucketName")
//      true
//    } recover {
//      case th: Throwable =>
//        Logger.error(s"error occurred while deleting file $objKey from bucket $bucketName", th)
//        false
//    }
//  }
//
//  def deleteObjects(bucketName: String, keys: String*): Future[Boolean] = {
//    if (keys.size == 1){
//      deleteObject(bucketName, keys(0))
//    }else {
//      deleteObjects(keys, bucketName)
//    }
//  }
//
//  def getPresignedURL(bucketName: String, objectKey: String, expiryDate: Date): Option[String] = {
//    Try {
//      val generatePresignedUrlRequest: GeneratePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, objectKey)
//      generatePresignedUrlRequest.setMethod(HttpMethod.GET)
//      generatePresignedUrlRequest.setExpiration(expiryDate)
//      val url = client.generatePresignedUrl(generatePresignedUrlRequest).toString
//      Logger.info(s"created pre-signed URL for [$bucketName:$objectKey] with expiry $expiryDate")
//      url
//    } match {
//      case Success(bf) => Some(bf)
//      case Failure(th) =>
//        Logger.error(s"error occurred while creating pre-signed url for file $objectKey from bucket $bucketName, ${th.getMessage}", th)
//        None
//    }
//  }
//
//  def getFilesKey(bucketName: String): Seq[String] = {
//    println(s"Reached in getFilesKey")
//    def getAll(objectListing: ObjectListing, keys: Seq[String]): Seq[String] = {
//      objectListing.isTruncated match {
//        case false => Logger.info(s"Object list is truncated from bucket")
//          keys
//        case true =>
//          val nextList = client.listNextBatchOfObjects(objectListing)
//          val nextSummaries = nextList.getObjectSummaries
//          val nextKeys = nextSummaries.map(_.getKey)
//          getAll(objectListing, keys ++ nextKeys)
//      }
//    }
//    val listing = client.listObjects(bucketName)
//    val summaries = listing.getObjectSummaries
//    val keys = summaries.map(_.getKey)
//    println(s"All keys befor loop: $keys , isTruncated = ${listing.isTruncated}")
//    getAll(listing, keys)
//  }
//
//}
//
//object S3Regions {
//
//  def getRegion(text: String): Regions = {
//    text.trim.toLowerCase match {
//      case "oregon"    => Regions.US_WEST_2
//      case "singapore" => Regions.AP_SOUTHEAST_1
//      case "default"   => Regions.US_EAST_1
//      case _           => Regions.US_EAST_1
//    }
//  }
//
//}
//
//object S3Client {
//  def apply(region: Regions, retryCount: Int, ec: ExecutionContext) =
//    new S3Client(region, retryCount, ec)
//}