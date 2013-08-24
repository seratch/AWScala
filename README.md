AWScala: AWS SDK on Scala REPL
=======

AWScala enables Scala developers to easily work with Amazon Web Services in the Scala way.

Though AWScala objects basically extend AWS SDK for Java APIs, you can use them　with less stress on Scala REPL or `sbt console`.

## Supported Services

http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/

- AWS Identity and Access Management (IAM)
- Amazon Simple Storage Service (Amazon S3)
- Amazon Simple Queue Service（Amazon SQS）

## Examples

### IAM

```scala
import awscala._, iam._
implicit val iam = IAM()

val group = iam.createGroup("Developers")
group.putPolicy("policy-name",
  """{
   |  "Version": "2012-10-17",
   |  "Statement": [
   |    {
   |      "Effect": "Allow",
   |      "Action": "s3:*",
   |      "Resource": "*"
   |    }
   |  ]
   |}
 """.stripMargin)

val user: User = iam.createUser("Alice")
user.setLoginPassword("password")
group.add(user)

group.policyNames.foreach(name => group.policy(name).destroy())
group.destroy()
```

https://github.com/seratch/awscala/blob/master/src/main/scala/awscala/iam/IAM.scala

https://github.com/seratch/awscala/blob/master/src/test/scala/awscala/IAMSpec.scala

### S3

```scala
import awscala._, s3._

implicit val s3 = S3()

val buckets: Seq[Bucket] = s3.buckets
val bucket: Bucket = s3.createBucket("unique-name-xxx")

s3.putObject(bucket, "sample.txt", new java.io.File("sample.txt"))

val s3obj: Option[S3Object] = s3.getObject(bucket, "sample.txt")

val obj = s3obj.get
obj.publicUrl // http://unique-name-xxx.s3.amazonaws.com/sample.txt
obj.generatePresignedUrl(DateTime.now.plusMinutes(10)) // ?Expires=....

s3obj.map(obj => s3.deleteObject(bucket, obj)) // or obj.destroy()

val summaries: Seq[S3ObjectSummary] = s3.objectSummaries(bucket)

s3.withBucket(bucket) { s3 =>
  val obj = s3.getObject("sample.txt")
  val summaries = s3.objectSummaries
  s3.deleteObject(obj)
}
```

https://github.com/seratch/awscala/blob/master/src/main/scala/awscala/s3/S3.scala

https://github.com/seratch/awscala/blob/master/src/test/scala/awscala/S3Spec.scala

### SQS

```scala
import awscala._, sqs._
implicit val sqs = SQS.at(Region.Tokyo)

val quques: Seq[Queue] = sqs.queues

val queue: Queue = sqs.createQueue("sample-queue")

sqs.sendMessage(queue, "message body")
sqs.sendMessages(queue, Seq("first", "second", "third"))

val messages: Seq[Message] = sqs.receiveMessage(queue)
messages.foreach(msg => sqs.deleteMessage(msg))
messages.foreach(_.destroy())

sqs.withQueue(queue) { sqs =>
  sqs.sendMessage("hello")
  val msgs = sqs.receiveMessage()
}

sqs.deleteQueue(queue)
```

https://github.com/seratch/awscala/blob/master/src/main/scala/awscala/sqs/SQS.scala

https://github.com/seratch/awscala/blob/master/src/test/scala/awscala/SQSSpec.scala

## How to contribute

If you're interested in contributing this project, please send pull requests!

### Running tests

Tests requires aws credentials with Administrator permissions:

```
export AWS_ACCESS_KEY_ID=xxx
export AWS_SECRET_ACCESS_KEY=yyy
```

And then, just run `sbt test`.

## License

Apache License, Version 2.0

