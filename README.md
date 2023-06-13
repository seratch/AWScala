## ⚠️ Important Notice ⚠️

This library is no longer maintained. If you would like to continue using it, please fork and update it on your own.

AWScala: AWS SDK on the Scala REPL
=======

[![Maven Central](https://img.shields.io/maven-central/v/com.github.seratch/awscala_2.13.svg?label=Maven%20Central)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.seratch%22%20a%3A%22awscala_2.13%22)

AWScala enables Scala developers to easily work with Amazon Web Services in the Scala way.

Though AWScala objects basically extend AWS SDK for Java APIs, you can use them with less stress on Scala REPL or `sbt console`.

## Supported Services

http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/

- AWS Identity and Access Management (IAM)
- AWS Security Token Service (STS)
- Amazon Elastic Compute Cloud (Amazon EC2)
- Amazon Simple Storage Service (Amazon S3)
- Amazon Simple Queue Service（Amazon SQS）
- Amazon Redshift
- Amazon DynamoDB
- Amazon SimpleDB
- AWS Step Functions

## How to use

To pull in all modules:

```scala
libraryDependencies += "com.github.seratch" %% "awscala" % "0.9.+"
```

To pull in only selected modules:

```scala
libraryDependencies ++= Seq(
    "com.github.seratch" %% "awscala-ec2" % "0.9.+",
    "com.github.seratch" %% "awscala-iam" % "0.9.+",
    "com.github.seratch" %% "awscala-dynamodb" % "0.9.+",
    "com.github.seratch" %% "awscala-emr" % "0.9.+",
    "com.github.seratch" %% "awscala-redshift" % "0.9.+",
    "com.github.seratch" %% "awscala-s3" % "0.9.+",
    "com.github.seratch" %% "awscala-simpledb" % "0.9.+",
    "com.github.seratch" %% "awscala-sqs" % "0.9.+",
    "com.github.seratch" %% "awscala-sts" % "0.9.+",
    "com.github.seratch" %% "awscala-stepfunctions" % "0.9.+"
)
```

Configure credentials in the AWS Java SDK way.

http://docs.aws.amazon.com/AWSSdkDocsJava/latest/DeveloperGuide/credentials.html

## Examples

### AWS Identity and Access Management (IAM)

```scala
import awscala._, iam._
implicit val iam = IAM()

val group = iam.createGroup("Developers")

group.putPolicy("policy-name", 
  Policy(Seq(Statement(Effect.Allow, Seq(Action("s3:*")), Seq(Resource("*"))))))

val user: User = iam.createUser("Alice")
user.setLoginPassword("password")
group.add(user)

group.policyNames.foreach(name => group.policy(name).destroy())
group.destroy()
```

https://github.com/seratch/AWScala/blob/master/iam/src/main/scala/awscala/iam/IAM.scala

https://github.com/seratch/awscala/blob/master/iam/src/test/scala/awscala/IAMSpec.scala

##### AWS Security Token Service (STS)

```scala
import awscala._, sts._

implicit val sts = STS()

val federation: FederationToken = sts.federationToken(
  name = "anonymous-user",
  policy = Policy(Seq(Statement(Effect.Allow, Seq(Action("s3:*")), Seq(Resource("*"))))),
  durationSeconds = 1200)

val signinToken: String = sts.signinToken(federation.credentials)

val loginUrl: String = sts.loginUrl(
  credentials = federation.credentials,
  consoleUrl  = "https://console.aws.amazon.com/iam",
  issuerUrl   = "http://example.com/internal/auth")
```

https://github.com/seratch/awscala/blob/master/sts/src/main/scala/awscala/sts/STS.scala

https://github.com/seratch/awscala/blob/master/sts/src/test/scala/awscala/STSSpec.scala


### Amazon Elastic Compute Cloud (Amazon EC2)

```scala
import awscala._, ec2._

implicit val ec2 = EC2.at(Region.Tokyo)

val existings: Seq[Instance] = ec2.instances

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global

// simply create a t1.micro instance
val f = Future(ec2.runAndAwait("ami-2819aa29", ec2.keyPairs.head))

for {
  instances <- f
  instance <- instances
} {
  instance.withKeyPair(new java.io.File("key_pair_file")) { i =>
    // optional: scala-ssh (https://github.com/sirthias/scala-ssh)
    i.ssh { ssh =>
      ssh.exec("ls -la").right.map { result =>
        println(s"------\n${inst.instanceId} Result:\n" + result.stdOutAsString())
      }
    }
  }
  instance.terminate()
}
```

https://github.com/seratch/awscala/blob/master/ec2/src/main/scala/awscala/ec2/EC2.scala

https://github.com/seratch/awscala/blob/master/ec2/src/test/scala/awscala/EC2Spec.scala


### Amazon Simple Storage Service (Amazon S3)

```scala
import awscala._, s3._

implicit val s3 = S3.at(Region.Tokyo)

val buckets: Seq[Bucket] = s3.buckets
val bucket: Bucket = s3.createBucket("unique-name-xxx")
val summaries: Seq[S3ObjectSummary] = bucket.objectSummaries

bucket.put("sample.txt", new java.io.File("sample.txt"))

val s3obj: Option[S3Object] = bucket.getObject("sample.txt")

s3obj.foreach { obj =>
  obj.publicUrl // http://unique-name-xxx.s3.amazonaws.com/sample.txt
  obj.generatePresignedUrl(DateTime.now.plusMinutes(10)) // ?Expires=....
  bucket.delete(obj) // or obj.destroy()
}
```

https://github.com/seratch/awscala/blob/master/s3/src/main/scala/awscala/s3/S3.scala

https://github.com/seratch/awscala/blob/master/s3/src/test/scala/awscala/S3Spec.scala

### Amazon Simple Queue Service（Amazon SQS）

```scala
import awscala._, sqs._
implicit val sqs = SQS.at(Region.Tokyo)

val queue: Queue = sqs.createQueue("sample-queue")

queue.add("message body")
queue.add("first", "second", "third")

val messages: Seq[Message] = queue.messages
queue.removeAll(messages)

queue.destroy()
```

https://github.com/seratch/awscala/blob/master/sqs/src/main/scala/awscala/sqs/SQS.scala

https://github.com/seratch/awscala/blob/master/sqs/src/test/scala/awscala/SQSSpec.scala

### Amazon Redshift

```scala
import awscala._, redshift._

implicit val redshift = Redshift.at(Region.Tokyo)

val cluster: Cluster = redshift.createCluster(NewCluster(
  "sample-cluster", "mydb", "username", "password"))

val snapshot: Snapshot = redshift.createSnapshot(cluster, "snapshot-name") 

redshift.delete(cluster, "final-snapshot-name")
```

https://github.com/seratch/awscala/blob/master/redshift/src/main/scala/awscala/redshift/Redshift.scala

### Amazon DynamoDB

```scala
import awscala._, dynamodbv2._ 

implicit val dynamoDB = DynamoDB.at(Region.Tokyo)

val tableMeta: TableMeta = dynamoDB.createTable(
  name = "Members",
  hashPK =  "Id" -> AttributeType.Number,
  rangePK = "Country" -> AttributeType.String,
  otherAttributes = Seq("Company" -> AttributeType.String),
  indexes = Seq(LocalSecondaryIndex(
    name = "CompanyIndex",
    keySchema = Seq(KeySchema("Id", KeyType.Hash), KeySchema("Company", KeyType.Range)),
    projection = Projection(ProjectionType.Include, Seq("Company"))
  ))
)

val table: Table = dynamoDB.table("Members").get

table.put(1, "Japan", "Name" -> "Alice", "Age" -> 23, "Company" -> "Google")
table.put(2, "U.S.",  "Name" -> "Bob",   "Age" -> 36, "Company" -> "Google")
table.put(3, "Japan", "Name" -> "Chris", "Age" -> 29, "Company" -> "Amazon")

val googlers: Seq[Item] = table.scan(Seq("Company" -> cond.eq("Google")))

table.destroy()
```

PUT method with case class usage (@hashPK and @rangePK annotations are not currently available in Scala 3)

```scala
import awscala._, dynamodbv2._ 

implicit val dynamoDB = DynamoDB.at(Region.Tokyo)

case class Member(Name: String, Age: Int, Company: String)
case class TestMember(
     @hashPK id: Int,
     @rangePK country: String,
     company: String,
     name: String,
     age: Int)
val tableMeta: TableMeta = dynamoDB.createTable(
  name = "Members",
  hashPK =  "Id" -> AttributeType.Number,
  rangePK = "Country" -> AttributeType.String,
  otherAttributes = Seq("Company" -> AttributeType.String),
  indexes = Seq(LocalSecondaryIndex(
    name = "CompanyIndex",
    keySchema = Seq(KeySchema("Id", KeyType.Hash), KeySchema("Company", KeyType.Range)),
    projection = Projection(ProjectionType.Include, Seq("Company"))
  ))
)

val table: Table = dynamoDB.table("Members").get
val member = Member("Alex", 29, "DataMass")
table.putItem(1, "PL", member)

// putItem() allows you to push the whole case class object with hashPK and rangePK included
val user = TestMember(2,"PL", "Jakub", 33, "DataMass")
table.putItem(user)

val members: Seq[Item] = table.scan(Seq("Company" -> cond.eq("DataMass")))
table.destroy()
```
 


https://github.com/seratch/awscala/blob/master/dynamodb/src/main/scala/awscala/dynamodbv2/DynamoDB.scala

https://github.com/seratch/awscala/blob/master/dynamodb/src/test/scala/awscala/DynamoDBV2Spec.scala

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

https://github.com/seratch/awscala/blob/master/simpledb/src/main/scala/awscala/simpledb/SimpleDB.scala

https://github.com/seratch/awscala/blob/master/simpledb/src/test/scala/awscala/SimpleDBSpec.scala

### AWS Step Functions

```scala
import awscala._, stepfunctions._

implicit val steps = StepFunctions.at(Region.Tokyo)

val machineDefinition = "{ ... state machine definition ... }"
val role = Role(...)

val machine = steps.createStateMachine("myMachine", machineDefinition, role)
val activity = steps.createActivity("MyActivity")
val exec = machine.startExecution("machine input")

steps.runActivity(activity.name) { input => s"Received input $input" }

exec.stepStatus("Some Step")
val history: Seq[ExecutionEvent] = exec.history()
val status = exec.status()

machine.delete()
activity.delete()
```

https://github.com/seratch/awscala/blob/master/stepfunctions/src/main/scala/awscala/stepfunctions/StepFunctions.scala

https://github.com/seratch/awscala/blob/master/stepfunctions/src/test/scala/awscala/StepFunctionsSpec.scala

### Amazon Elastic MapReduce (Amazon EMR)

Created by @CruncherBigData. If you have any feedback or questions, please contact @CruncherBigData.

https://github.com/seratch/awscala/blob/master/emr/src/main/scala/awscala/emr/EMR.scala

https://github.com/seratch/awscala/blob/master/emr/src/test/scala/awscala/EMRSpec.scala

## How to contribute

If you're interested in contributing this project, please send pull requests!

### Running tests

Tests require aws credentials with Administrator permissions:

```
export AWS_ACCESS_KEY_ID=xxx
export AWS_SECRET_ACCESS_KEY=yyy
```

The DynamoDB tests also require a locally running instance of DynamoDB.
An install script is provided as `bin/installDynamoDbLocal`.
A launch script is provided as `bin/runDynamoDbLocal`.
See http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/Tools.DynamoDBLocal.html for more info.

To run the tests, just type `sbt test`.

## License

Copyright 2013 - AWScala Developers

Apache License, Version 2.0

