package awscala

import awscala._, s3._

import org.slf4j._
import org.scalatest._
import org.scalatest.matchers._

class S3Spec extends FlatSpec with ShouldMatchers {

  behavior of "S3"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "provide cool APIs" in {
    implicit val s3 = S3.at(Region.Tokyo)

    // buckets
    val buckets = s3.buckets
    log.info(s"Buckets: ${buckets}")

    val newBucketName = s"awscala-unit-test-${System.currentTimeMillis}"
    val bucket = s3.createBucket(newBucketName)
    log.info(s"Created Bucket: ${bucket}")

    // create/update objectes
    s3.put(bucket, "S3.scala", new java.io.File("src/main/scala/awscala/s3/S3.scala"))
    s3.putAsPublicRead(bucket, "S3.scala", new java.io.File("src/main/scala/awscala/s3/S3.scala"))
    s3.put(bucket, "S3Spec.scala", new java.io.File("src/test/scala/awscala/S3Spec.scala"))

    // get objects
    val obj = s3.get(bucket, "S3.scala")
    log.info(s"Object: ${obj}")
    val summaries = s3.objectSummaries(bucket)
    log.info(s"Object Summaries: ${summaries}")

    // delete objects
    s3.delete(obj.get)
    s3.get(bucket, "S3Spec.scala").map(_.destroy()) // working with implicit S3 isntance

    s3.withBucket(bucket) { s3q =>
      s3q.keys.map(key => s3q.get(key).map(_.destroy()))
    }

    bucket.destroy()
  }

}
