package awscala

import awscala._, s3._

import org.slf4j._
import org.scalatest._

class S3Spec extends FlatSpec with Matchers {

  behavior of "S3"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "handle buckets with > 1000 objects in them " in {
    implicit val s3 = S3.at(Region.Tokyo)

    // buckets
    val buckets = s3.buckets
    log.info(s"Buckets: ${buckets}")

    val newBucketName = s"awscala-unit-test-${System.currentTimeMillis}"
    val bucket = s3.createBucket(newBucketName)
    log.info(s"Created Bucket: ${bucket}")

    // create/update objects
    val file = new java.io.File("s3/src/main/scala/awscala/s3/S3.scala")
    for (i <- 1 to 1002) {
      bucket.put("S3.scala-" + i, file)
    }

    // delete objects
    val summaries = bucket.objectSummaries().toList

    summaries foreach {
      o => { log.info("deleting ${o.getKey}"); s3.deleteObject(bucket.name, o.getKey) }
    }
    bucket.destroy()
  }

  it should "provide cool APIs" in {
    implicit val s3 = S3.at(Region.Tokyo)

    // buckets
    val buckets = s3.buckets
    log.info(s"Buckets: ${buckets}")

    val newBucketName = s"awscala-unit-test-${System.currentTimeMillis}"
    val bucket = s3.createBucket(newBucketName)
    log.info(s"Created Bucket: ${bucket}")

    // create/update objects
    bucket.put("S3.scala", new java.io.File("s3/src/main/scala/awscala/s3/S3.scala"))
    bucket.putAsPublicRead("S3.scala", new java.io.File("s3/src/main/scala/awscala/s3/S3.scala"))
    bucket.put("S3Spec.scala", new java.io.File("src/test/scala/awscala/S3Spec.scala"))

    // get objects
    val s3obj: Option[S3Object] = bucket.get("S3.scala")
    log.info(s"Object: ${s3obj}")
    val summaries = bucket.objectSummaries
    log.info(s"Object Summaries: ${summaries}")

    // delete objects
    s3obj.foreach(o => { o.content.close(); bucket.delete(o) })
    bucket.get("S3Spec.scala").foreach { o => o.content.close(); o.destroy() } // working with implicit S3 instance

    bucket.destroy()
  }

}
