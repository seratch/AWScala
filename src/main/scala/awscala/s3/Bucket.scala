package awscala.s3

import scala.collection.JavaConverters._
import com.amazonaws.services.{ s3 => aws }

object Bucket {

  def apply(underlying: com.amazonaws.services.s3.model.Bucket): Bucket = {
    val bucket = new Bucket(underlying.getName)
    bucket.setCreationDate(underlying.getCreationDate)
    bucket.setOwner(underlying.getOwner)
    bucket
  }
}

case class Bucket(name: String) extends aws.model.Bucket(name) {

  def policy()(implicit s3: S3) = s3.policy(this)
  def policy(text: String)(implicit s3: S3) = s3.policy(this).setPolicyText(text)

  def acl()(implicit s3: S3) = s3.acl(this)
  def acl(acl: AccessControlList)(implicit s3: S3) = s3.bucketAcl(this, acl)

  def crossOriginConfig()(implicit s3: S3) = s3.crossOriginConfig(this)
  def lifecycleConfig(bucket: Bucket)(implicit s3: S3) = s3.lifecycleConfig(this)
  def loggingConfig(bucket: Bucket)(implicit s3: S3) = s3.loggingConfig(this)
  def notificationConfig(bucket: Bucket)(implicit s3: S3) = s3.notificationConfig(this)
  def taggingConfig(bucket: Bucket)(implicit s3: S3) = s3.taggingConfig(this)
  def versioningConfig(bucket: Bucket)(implicit s3: S3) = s3.versioningConfig(this)
  def websiteConfig(bucket: Bucket)(implicit s3: S3) = s3.websiteConfig(this)

  def destroy()(implicit s3: S3): Unit = s3.deleteBucket(name)
}

object BucketPolicy {
  def apply(bucket: Bucket, bp: aws.model.BucketPolicy): BucketPolicy = BucketPolicy(bucket, bp.getPolicyText)
}

case class BucketPolicy(bucket: Bucket, policyText: String) extends aws.model.BucketPolicy {
  setPolicyText(policyText)
}

object BucketCrossOriginConfiguration {
  def apply(bucket: Bucket, c: aws.model.BucketCrossOriginConfiguration): BucketCrossOriginConfiguration = {
    BucketCrossOriginConfiguration(bucket, c.getRules.asScala)
  }
}
case class BucketCrossOriginConfiguration(bucket: Bucket, rules: Seq[aws.model.CORSRule])
    extends aws.model.BucketCrossOriginConfiguration(rules.asJava) {

  def destroy()(implicit s3: S3) = s3.deleteCrossOriginConfig(bucket)
}

object BucketLifecycleConfiguration {
  def apply(bucket: Bucket, c: aws.model.BucketLifecycleConfiguration): BucketLifecycleConfiguration = {
    BucketLifecycleConfiguration(bucket, c.getRules.asScala)
  }
}
case class BucketLifecycleConfiguration(bucket: Bucket, rules: Seq[aws.model.BucketLifecycleConfiguration.Rule])
    extends aws.model.BucketLifecycleConfiguration(rules.asJava) {

  def destroy()(implicit s3: S3) = s3.deleteLifecycleConfig(bucket)
}

object BucketLoggingConfiguration {
  def apply(c: aws.model.BucketLoggingConfiguration): BucketLoggingConfiguration = {
    BucketLoggingConfiguration(Bucket(c.getDestinationBucketName), c.getLogFilePrefix)
  }
}
case class BucketLoggingConfiguration(bucket: Bucket, logFilePrefix: String)
  extends aws.model.BucketLoggingConfiguration(bucket.name, logFilePrefix)

object BucketNotificationConfiguration {
  def apply(bucket: Bucket, c: aws.model.BucketNotificationConfiguration): BucketNotificationConfiguration = {
    BucketNotificationConfiguration(bucket, c.getTopicConfigurations.asScala)
  }
}
case class BucketNotificationConfiguration(
  bucket: Bucket, topicConfigs: Seq[aws.model.BucketNotificationConfiguration.TopicConfiguration])
    extends aws.model.BucketNotificationConfiguration(topicConfigs.asJava)

object BucketTaggingConfiguration {
  def apply(bucket: Bucket, c: aws.model.BucketTaggingConfiguration): BucketTaggingConfiguration = {
    BucketTaggingConfiguration(bucket, c.getAllTagSets.asScala)
  }
}
case class BucketTaggingConfiguration(bucket: Bucket, tagSets: Seq[aws.model.TagSet])
    extends aws.model.BucketTaggingConfiguration(tagSets.asJava) {

  def destroy()(implicit s3: S3) = s3.deleteTaggingConfig(bucket)
}

object BucketVersioningConfiguration {
  def apply(bucket: Bucket, c: aws.model.BucketVersioningConfiguration): BucketVersioningConfiguration = {
    BucketVersioningConfiguration(bucket, c.getStatus, c.isMfaDeleteEnabled)
  }
}
case class BucketVersioningConfiguration(bucket: Bucket, status: String, mfaDeleteEnabled: Boolean)
    extends aws.model.BucketVersioningConfiguration {
  setStatus(status)
  setMfaDeleteEnabled(mfaDeleteEnabled)
}

object BucketWebsiteConfiguration {
  def apply(bucket: Bucket, c: aws.model.BucketWebsiteConfiguration): BucketWebsiteConfiguration = {
    BucketWebsiteConfiguration(bucket, c.getIndexDocumentSuffix, c.getErrorDocument, c.getRoutingRules.asScala)
  }
}
case class BucketWebsiteConfiguration(
  bucket: Bucket, indexDocumentSuffix: String, errorDocument: String, routingRules: Seq[aws.model.RoutingRule])
    extends aws.model.BucketWebsiteConfiguration(indexDocumentSuffix, errorDocument) {
  setRoutingRules(routingRules.asJava)

  def destroy()(implicit s3: S3) = s3.deleteWebsiteConfig(bucket)
}

