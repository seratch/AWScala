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
    serverSideEncryption = obj.getSSEAlgorithm
  )

  def apply(bucket: Bucket, key: String, obj: aws.model.PutObjectResult): PutObjectResult = new PutObjectResult(
    bucket = bucket,
    key = key,
    versionId = obj.getVersionId,
    eTag = obj.getETag,
    contentMd5 = obj.getContentMd5,
    expirationTime = new DateTime(obj.getExpirationTime),
    expirationTimeRuleId = obj.getExpirationTimeRuleId,
    serverSideEncryption = obj.getSSEAlgorithm
  )
}

case class PutObjectResult(bucket: Bucket, key: String, versionId: String,
  eTag: String, contentMd5: String, expirationTime: DateTime, expirationTimeRuleId: String, serverSideEncryption: String)
    extends aws.model.PutObjectResult {

  setVersionId(versionId)
  setETag(eTag)
  setContentMd5(contentMd5)
  setExpirationTime(expirationTime.toDate)
  setExpirationTimeRuleId(expirationTimeRuleId)
  setSSEAlgorithm(serverSideEncryption)
}

