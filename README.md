AWScala: AWS SDK on Scala REPL
=======

AWScala enables Scala developers to easily work with Amazon Web Services in the Scala way.

Though AWScala objects basically extend AWS SDK for Java APIs, you can use them with less stress on Scala REPL or `sbt console`.

## Supported Services

http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/

- AWS Identity and Access Management (IAM)
- Amazon Simple Storage Service (Amazon S3)
- Amazon Simple Queue Service（Amazon SQS）
- Amazon DynamoDB
- Amazon SimpleDB

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

### Amazon Simple Queue Service（Amazon SQS）

```scala
import awscala._, sqs._
implicit val sqs = SQS.at(Region.Tokyo)

val queue: Queue = sqs.createQueue("sample-queue")

queue.add("message body")
qeueu.add("first", "second", "third")

val messages: Seq[Message] = queue.messages
queue.removeAll(messages)

queue.destroy()
```

https://github.com/seratch/awscala/blob/master/src/main/scala/awscala/sqs/SQS.scala

https://github.com/seratch/awscala/blob/master/src/test/scala/awscala/SQSSpec.scala

### Amazon DynamoDB

```scala
import awscala._, dynamodb._

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

val googlers: Seq[Item] = table.scan(Seq("Company" -> Condition.gt("Google")))

table.destroy()
```

https://github.com/seratch/awscala/blob/master/src/main/scala/awscala/dynamodb/DynamoDB.scala

https://github.com/seratch/awscala/blob/master/src/test/scala/awscala/DynamoDBSpec.scala

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

