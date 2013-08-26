AWScala: AWS SDK on Scala REPL
=======

AWScala enables Scala developers to easily work with Amazon Web Services in the Scala way.

Though AWScala objects basically extend AWS SDK for Java APIs, you can use them with less stress on Scala REPL or `sbt console`.

## Supported Services

http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/

- AWS Identity and Access Management (IAM)
- Amazon Simple Storage Service (Amazon S3)
- Amazon SimpleDB
- Amazon Simple Queue Service（Amazon SQS）

## Examples

### AWS Identity and Access Management (IAM)

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

### Amazon Simple Storage Service (Amazon S3)

```scala
import awscala._, s3._

implicit val s3 = S3()

val buckets: Seq[Bucket] = s3.buckets
val bucket: Bucket = s3.createBucket("unique-name-xxx")
val summaries: Seq[S3ObjectSummary] = bucket.objectSummaries

bucket.put("sample.txt", new java.io.File("sample.txt"))

val s3obj: Option[S3Object] = bucket.getObject("sample.txt")

s3obj.foreach { obj =>
  obj.publicUrl // http://unique-name-xxx.s3.amazonaws.com/sample.txt
  obj.get.generatePresignedUrl(DateTime.now.plusMinutes(10)) // ?Expires=....
  bucket.delete(obj) // or obj.destroy()
}
```

https://github.com/seratch/awscala/blob/master/src/main/scala/awscala/s3/S3.scala

https://github.com/seratch/awscala/blob/master/src/test/scala/awscala/S3Spec.scala

### Amazon SimpleDB

```scala
import awscala._, simpledb._

implicit val simpleDB = SimpleDB.at(Region.Tokyo)

val domain: Domain = simpleDB.createDomain("users")

domain.put("00001", "name" -> "Alice", "age" -> "23", "country" -> "America")
domain.put("00002", "name" -> "Bob",   "age" -> "34", "country" -> "America")
domain.put("00003", "name" -> "Chris", "age" -> "27", "country" -> "Japan")

val items: Seq[Item] = domain.select(s"select * from users where country = 'America'")

simpleDB.domains.foreach(_.destroy())
```

https://github.com/seratch/awscala/blob/master/src/main/scala/awscala/simpledb/SimpleDB.scala

https://github.com/seratch/awscala/blob/master/src/test/scala/awscala/SimpleDBSpec.scala

### Amazon Simple Queue Service（Amazon SQS）

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

###Amazon Elastic Compute Cloud (Amazon EC2)

```scala
import awscala._, ec2._

implicit val ec2 = EC2.at(Region.Tokyo)

val existings:Seq[Instance] = ec2.instances

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

val kpFile:java.io.File = new java.io.File("YOUR_KEY_PAIR_PATH")
val f = Future(ec2.run(RunInstancesRequest("ami-2819aa29").withKeyName("YOUR_KEY_PAIR_NAME").withInstanceType("t1.micro")))

for{
	instances <- f
	inst <- instances
} inst.withKeyPair(kpFile){i =>
	inst.ssh{
	    client=>
	    client.exec("ls -la").right.map { result =>
   	        println(s"------\n${inst.instanceId} Result:\n" + result.stdOutAsString())
        }
   }
   i.terminate
}
```

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

