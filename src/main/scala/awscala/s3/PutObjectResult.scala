package awscala.s3

import awscala._
import com.amazonaws.services.{ s3 => aws }

object PutObjectResult {

  def apply(bucket: Bucket, key: String, obj: aws.model.CopyObjectResult): PutObjectResult = new PutObjectResult(
    bucket = bucket,
    key = key,
    versionId = obj.getVersionId,
    eTag = obj.getETag,
    contentMd5 = null,
    expirationTime = new DateTime(obj.getExpirationTime),
    expirationTimeRuleId = obj.getExpirationTimeRuleId,
    sseAlgorithm = obj.getSSEAlgorithm
  )

  def apply(bucket: Bucket, key: String, obj: aws.model.PutObjectResult): PutObjectResult = new PutObjectResult(
    bucket = bucket,
    key = key,
    versionId = obj.getVersionId,
    eTag = obj.getETag,
    contentMd5 = obj.getContentMd5,
    expirationTime = new DateTime(obj.getExpirationTime),
    expirationTimeRuleId = obj.getExpirationTimeRuleId,
    sseAlgorithm = obj.getSSEAlgorithm
  )
}

case class PutObjectResult(bucket: Bucket, key: String, versionId: String,
  eTag: String, contentMd5: String, expirationTime: DateTime, expirationTimeRuleId: String, sseAlgorithm: String)
    extends aws.model.PutObjectResult {

  @deprecated("aws.model.PutObjectResult#setServerSideEncryption/getServerSideEncryption is deprecated in AWS SDK 1.8.0.", "0.2.6")
  val serverSideEncryption = sseAlgorithm
  setVersionId(versionId)
  setETag(eTag)
  setContentMd5(contentMd5)
  setExpirationTime(expirationTime.toDate)
  setExpirationTimeRuleId(expirationTimeRuleId)
  setSSEAlgorithm(sseAlgorithm)
}

