package awscala.s3

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ s3 => aws }
import java.io.{ File, ByteArrayInputStream }
import aws.model.{ Region => S3Region }

object S3 {

  def apply(credentials: Credentials = Credentials.defaultEnv): S3 = new S3Client(credentials)
  def apply(accessKeyId: String, secretAccessKey: String): S3 = apply(Credentials(accessKeyId, secretAccessKey))

  def at(region: Region, s3Region: S3Region = S3Region.US_Standard): S3 = apply().at(region).at(s3Region)
}

/**
 * Amazon S3 Java client wrapper
 * @see "http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/"
 */
trait S3 extends aws.AmazonS3 {

  private[this] var s3Region = S3Region.US_Standard

  def at(region: Region): S3 = {
    this.setRegion(region)
    this
  }

  def at(s3Region: S3Region): S3 = {
    this.s3Region = s3Region
    this
  }

  def s3AccountOwner: Owner = Owner(getS3AccountOwner)

  // ------------------------------------------
  // Buckets
  // ------------------------------------------

  def buckets: Seq[Bucket] = listBuckets.asScala.toSeq.map(b => Bucket(b))
  def bucket(name: String): Option[Bucket] = buckets.find(_.name == name)

  def acl(bucket: Bucket): AccessControlList = bucketAcl(bucket.name)
  def bucketAcl(name: String): AccessControlList = AccessControlList(getBucketAcl(name))
  def bucketAcl(bucket: Bucket, acl: AccessControlList) = setBucketAcl(bucket.name, acl)

  def location(bucket: Bucket): String = getBucketLocation(bucket.name)

  def crossOriginConfig(bucket: Bucket) = BucketCrossOriginConfiguration(bucket, getBucketCrossOriginConfiguration(bucket.name))
  def lifecycleConfig(bucket: Bucket) = BucketLifecycleConfiguration(bucket, getBucketLifecycleConfiguration(bucket.name))
  def policy(bucket: Bucket) = BucketPolicy(bucket, getBucketPolicy(bucket.name))
  def loggingConfig(bucket: Bucket) = BucketLoggingConfiguration(getBucketLoggingConfiguration(bucket.name))
  def notificationConfig(bucket: Bucket) = BucketNotificationConfiguration(bucket, getBucketNotificationConfiguration(bucket.name))
  def taggingConfig(bucket: Bucket) = BucketTaggingConfiguration(bucket, getBucketTaggingConfiguration(bucket.name))
  def versioningConfig(bucket: Bucket) = BucketVersioningConfiguration(bucket, getBucketVersioningConfiguration(bucket.name))
  def websiteConfig(bucket: Bucket) = BucketWebsiteConfiguration(bucket, getBucketWebsiteConfiguration(bucket.name))

  def createBucket(name: String): Bucket = Bucket(createBucket(new aws.model.CreateBucketRequest(name, s3Region)))

  def delete(bucket: Bucket): Unit = deleteBucket(bucket)
  def deleteBucket(bucket: Bucket): Unit = deleteBucket(bucket.name)

  def deleteCrossOriginConfig(bucket: Bucket): Unit = deleteBucketCrossOriginConfiguration(bucket.name)
  def deleteLifecycleConfig(bucket: Bucket): Unit = deleteBucketLifecycleConfiguration(bucket.name)
  def deletePolicy(bucket: Bucket): Unit = deleteBucketPolicy(bucket.name)
  def deleteTaggingConfig(bucket: Bucket): Unit = deleteBucketTaggingConfiguration(bucket.name)
  def deleteWebsiteConfig(bucket: Bucket): Unit = deleteBucketWebsiteConfiguration(bucket.name)

  // ------------------------------------------
  // Objects
  // ------------------------------------------

  // get
  def get(bucket: Bucket, key: String) = getObject(bucket, key)
  def get(bucket: Bucket, key: String, versionId: String) = getObject(bucket, key, versionId)
  def getObject(bucket: Bucket, key: String): Option[S3Object] = try {
    Option(getObject(new aws.model.GetObjectRequest(bucket.name, key))).map(obj => S3Object(bucket, obj))
  } catch { case e: aws.model.AmazonS3Exception => None }
  def getObject(bucket: Bucket, key: String, versionId: String): Option[S3Object] = try {
    Option(getObject(new aws.model.GetObjectRequest(bucket.name, key, versionId))).map(obj => S3Object(bucket, obj))
  } catch { case e: aws.model.AmazonS3Exception => None }

  def metadata(bucket: Bucket, key: String) = getObjectMetadata(bucket.name, key)

  // listObjects
  def objectSummaries(bucket: Bucket): Seq[S3ObjectSummary] = {
    listObjects(bucket.name).getObjectSummaries.asScala.map(s => S3ObjectSummary(bucket, s)).toSeq
  }
  def objectSummaries(bucket: Bucket, prefix: String): Seq[S3ObjectSummary] = {
    listObjects(bucket.name, prefix).getObjectSummaries.asScala.map(s => S3ObjectSummary(bucket, s)).toSeq
  }
  def keys(bucket: Bucket): Seq[String] = objectSummaries(bucket).map(os => os.getKey)
  def keys(bucket: Bucket, prefix: String): Seq[String] = objectSummaries(bucket, prefix).map(os => os.getKey)

  // acl
  def acl(obj: S3Object): AccessControlList = acl(obj.bucket, obj.key)
  def acl(bucket: Bucket, key: String): AccessControlList = AccessControlList(getObjectAcl(bucket.name, key))

  def acl(obj: S3Object, acl: AccessControlList): Unit = setObjectAcl(obj.bucket.name, obj.key, acl)
  def acl(obj: S3Object, acl: CannedAccessControlList): Unit = setObjectAcl(obj.bucket.name, obj.key, acl)

  def acl(bucket: Bucket, key: String, acl: AccessControlList): Unit = setObjectAcl(bucket.name, key, acl)
  def acl(bucket: Bucket, key: String, acl: CannedAccessControlList): Unit = setObjectAcl(bucket.name, key, acl)

  // put
  def put(bucket: Bucket, key: String, file: File): PutObjectResult = putObject(bucket, key, file)
  def putAsPublicRead(bucket: Bucket, key: String, file: File): PutObjectResult = putObjectAsPublicRead(bucket, key, file)
  def putAsPublicReadWrite(bucket: Bucket, key: String, file: File): PutObjectResult = putObjectAsPublicReadWrite(bucket, key, file)

  def putObject(bucket: Bucket, key: String, file: File): PutObjectResult = PutObjectResult(bucket, key, putObject(bucket.name, key, file))
  def putObjectAsPublicRead(bucket: Bucket, key: String, file: File): PutObjectResult = {
    PutObjectResult(bucket, key, putObject(
      new aws.model.PutObjectRequest(bucket.name, key, file).withCannedAcl(aws.model.CannedAccessControlList.PublicRead)))
  }
  def putObjectAsPublicReadWrite(bucket: Bucket, key: String, file: File): PutObjectResult = {
    PutObjectResult(bucket, key, putObject(
      new aws.model.PutObjectRequest(bucket.name, key, file).withCannedAcl(aws.model.CannedAccessControlList.PublicReadWrite)))
  }

  // putting a byte array
  def put(bucket: Bucket, key: String, bytes: Array[Byte], metadata: aws.model.ObjectMetadata): PutObjectResult = putObject(bucket, key, bytes, metadata)
  def putAsPublicRead(bucket: Bucket, key: String, bytes: Array[Byte], metadata: aws.model.ObjectMetadata): PutObjectResult = putObjectAsPublicRead(bucket, key, bytes, metadata)

  def putObject(bucket: Bucket, key: String, bytes: Array[Byte], metadata: aws.model.ObjectMetadata): PutObjectResult =
    PutObjectResult(bucket, key, putObject(
      new aws.model.PutObjectRequest(bucket.name, key, new ByteArrayInputStream(bytes), metadata)
    ))

  def putObjectAsPublicRead(bucket: Bucket, key: String, bytes: Array[Byte], metadata: aws.model.ObjectMetadata): PutObjectResult = {
    PutObjectResult(bucket, key, putObject(
      new aws.model.PutObjectRequest(bucket.name, key,
        new ByteArrayInputStream(bytes),
        metadata
      ).withCannedAcl(aws.model.CannedAccessControlList.PublicRead))
    )
  }

  // copy
  def copy(from: S3Object, to: S3Object): PutObjectResult = copyObject(from, to)
  def copyObject(from: S3Object, to: S3Object): PutObjectResult = {
    val result = copyObject(from.bucket.name, from.key, to.bucket.name, to.key)
    PutObjectResult(to.bucket, to.key, result)
  }

  // delete
  def delete(obj: S3Object): Unit = deleteObject(obj)
  def deleteObject(obj: S3Object): Unit = deleteObject(obj.bucket.name, obj.key)
  def deleteVersion(obj: S3Object, versionId: String): Unit = deleteObjectVersion(obj, versionId)
  def deleteObjectVersion(obj: S3Object, versionId: String): Unit = {
    deleteVersion(new aws.model.DeleteVersionRequest(obj.bucket.name, obj.key, versionId))
  }
  def deleteObjects(objs: Seq[S3Object]): Unit = objs.headOption.map { obj =>
    val req = new aws.model.DeleteObjectsRequest(obj.bucket.name)
    req.setKeys(objs.map(obj => new aws.model.DeleteObjectsRequest.KeyVersion(obj.key, obj.versionId)).asJava)
    deleteObjects(req)
  }

  // presignedUrl
  def generatePresignedUrl(obj: S3Object, expiration: DateTime): java.net.URL = {
    generatePresignedUrl(obj.bucket.name, obj.key, expiration.toDate)
  }

}

/**
 * Default Implementation
 *
 * @param credentials credentials
 */
class S3Client(credentials: Credentials = Credentials.defaultEnv)
    extends aws.AmazonS3Client(credentials)
    with S3 {

  override def createBucket(name: String): Bucket = super.createBucket(name)
}
