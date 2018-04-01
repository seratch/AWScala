package awscala.s3

import awscala._
import com.amazonaws.services.{ s3 => aws }

object S3Object {

  def apply(bucket: Bucket, obj: aws.model.S3Object): S3Object = new S3Object(
    bucket = bucket,
    key = obj.getKey,
    content = obj.getObjectContent,
    redirectLocation = obj.getRedirectLocation,
    metadata = obj.getObjectMetadata)
}

case class S3Object(
  bucket: Bucket, key: String, content: java.io.InputStream = null,
  redirectLocation: String = null, metadata: aws.model.ObjectMetadata = null)
  extends aws.model.S3Object {

  setBucketName(bucket.name)
  setKey(key)
  setObjectContent(content)
  setRedirectLocation(redirectLocation)
  setObjectMetadata(metadata)

  import aws.model.{ CannedAccessControlList => CannedACL }

  def setAsAuthenticatedRead()(implicit s3: S3) = s3.acl(this, CannedACL.AuthenticatedRead)
  def setAsBucketOwnerFullControl()(implicit s3: S3) = s3.acl(this, CannedACL.BucketOwnerFullControl)
  def setAsBucketOwnerRead()(implicit s3: S3) = s3.acl(this, CannedACL.BucketOwnerRead)
  def setAsLogDeliveryWrite()(implicit s3: S3) = s3.acl(this, CannedACL.LogDeliveryWrite)
  def setAsPrivate()(implicit s3: S3) = s3.acl(this, CannedACL.Private)
  def setAsPublicRead()(implicit s3: S3) = s3.acl(this, CannedACL.PublicRead)
  def setAsPublicReadWrite()(implicit s3: S3) = s3.acl(this, CannedACL.PublicReadWrite)

  def publicUrl: java.net.URL = new java.net.URL(s"http://${bucket.name}.s3.amazonaws.com/${key}")

  def generatePresignedUrl(expiration: DateTime)(implicit s3: S3): java.net.URL = {
    s3.generatePresignedUrl(this, expiration)
  }

  lazy val versionId: String = Option(metadata).map(_.getVersionId).getOrElse(null)

  def destroy()(implicit s3: S3): Unit = s3.deleteObject(this)
}
