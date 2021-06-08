package awscala.s3

import awscala.DateTime.toDate
import awscala._
import com.amazonaws.services.{ s3 => aws }

object S3ObjectSummary {

  def apply(bucket: Bucket, obj: aws.model.S3ObjectSummary): S3ObjectSummary = new S3ObjectSummary(
    bucket = bucket,
    key = obj.getKey,
    size = obj.getSize,
    storageClass = obj.getStorageClass,
    eTag = obj.getETag,
    lastModified = DateTime(obj.getLastModified),
    owner = obj.getOwner)
}

class S3ObjectSummary(val bucket: Bucket, key: String, size: Long,
  storageClass: String, eTag: String, lastModified: DateTime, owner: aws.model.Owner)
  extends aws.model.S3ObjectSummary {

  setBucketName(bucket.name)
  setKey(key)
  setSize(size)
  setStorageClass(storageClass)
  setETag(eTag)
  setLastModified(toDate(lastModified))
  setOwner(owner)
}

