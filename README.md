AWScala: AWS SDK on Scala REPL
=======

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

## How to use

```scala
libraryDependencies += "com.github.seratch" %% "awscala" % "[0.1,)"
```

## Examples

### AWS Identity and Access Management (IAM)

```scala
import awscala._, iam._
implicit val iam = IAM()

val group = iam.createGroup("Developers")

import awscala.auth.policy._
group.putPolicy("policy-name", 
  Policy(Seq(Statement(Effect.Allow, Seq(Action("s3:*")), Seq(Resource("*"))))))

val user: User = iam.createUser("Alice")
user.setLoginPassword("password")
group.add(user)

group.policyNames.foreach(name => group.policy(name).destroy())
group.destroy()
```

https://github.com/seratch/awscala/blob/master/src/main/scala/awscala/iam/IAM.scala

https://github.com/seratch/awscala/blob/master/src/test/scala/awscala/IAMSpec.scala

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

https://github.com/seratch/awscala/blob/master/src/main/scala/awscala/sts/STS.scala

https://github.com/seratch/awscala/blob/master/src/test/scala/awscala/STSSpec.scala


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

https://github.com/seratch/awscala/blob/master/src/main/scala/awscala/ec2/EC2.scala

https://github.com/seratch/awscala/blob/master/src/test/scala/awscala/EC2Spec.scala


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

### Amazon Redshift

```scala
import awscala._, redshift._

implicit val redshift = Redshift.at(Region.Tokyo)

val cluster: Cluster = redshift.createCluster(NewCluster(
  "sample-cluster", "mydb", "username", "password"))

val snapshot: Snapshot = redshift.createSnapshot(cluster, "snapshot-name") 

redshift.delete(cluster, "final-snapshot-name")
```

https://github.com/seratch/awscala/blob/master/src/main/scala/awscala/redshift/Redshift.scala

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

val googlers: Seq[Item] = table.scan(Seq("Company" -> Condition.gt("Google")))

table.destroy()
```

https://github.com/seratch/awscala/blob/master/src/main/scala/awscala/dynamodbv2/DynamoDB.scala

https://github.com/seratch/awscala/blob/master/src/test/scala/awscala/DynamoDBV2Spec.scala

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

### Amazon Elastic MapReduce (Amazon EMR)

```scala
import awscala._
import scala.collection.JavaConversions._

 implicit val emr = EMR()
     //cluster nodes information
    val masterInstanceType = "c1.medium"
    val masterMarketType = "ON_DEMAND"
    val masterBidPrice = "0.00"
    val coreInstanceType = "c1.medium"
    val coreInstanceCount = 1
    val coreMarketType = "ON_DEMAND"
    val coreBidPrice = "0.00"
    val taskInstanceType = "c1.medium"
    val taskInstanceCount = 1
    val taskMarketType = "ON_DEMAND"
    val taskBidPrice = "0.00"
    val ec2KeyName = "ec2KeyName"
    val hadoopVersion = "1.0.3"
    //job settings
    val jobName = "My Test Job"
    val amiVersion = "latest"
    val loggingURI = "s3://path/to/log/dir/"
    val visibleToAllUsers = true
    //individual steps information      
    val step1 = emr.jarStep("step1", "jarStep", "s3://path/to/jar/file", "com.myclass", List("--key1", "value1", "--key2", "value2"))
    val step2 = emr.jarStep("step2", "jarStep", "s3://path/to/jar/file", "com.myclass", List("--key1", "value1", "--key2", "value2"))
    val steps = List(step1, step2)

    //build and EMR cluster
    val jobFlowInstancesConfig = emr.buildJobFlowInstancesConfig(
    masterInstanceType, 
    masterMarketType, 
    masterBidPrice, 
    coreInstanceType, 
    coreInstanceCount, 
    coreMarketType, 
    coreBidPrice, 
    taskInstanceType, 
    taskInstanceCount, 
    taskMarketType, 
    taskBidPrice, 
    ec2KeyName, 
    hadoopVersion)

    // Add map reduce jobs to the cluster
    val jobFlowStepsRequest = emr.buildJobFlowStepsRequest(steps)
    
    // prepare the cluster to run 
    val runJobFlowRequest = emr.buildRunRequest(
    jobName, 
    amiVersion, 
    loggingURI, 
    visibleToAllUsers, 
    jobFlowInstancesConfig, 
    jobFlowStepsRequest)
    
    // run the cluster    
    val runJobFlowResult = emr.runJobFlow(runJobFlowRequest)
    
    //obtain job flow ID once the cluster is in STARTING state. 
    val job_flow_id = runJobFlowResult.getJobFlowId()
    
    
    Or , you can access the runJobFlow method directly which will take care of object creation, note how all the parameters are primitive types (facade design pattern). Here is an example: 
    val run_request = emr.runJobFlow(
      masterInstanceType, 
      masterMarketType, 
      masterBidPrice, 
      coreInstanceType, 
      coreInstanceCount, 
      coreMarketType, 
      coreBidPrice, 
      taskInstanceType, 
      taskInstanceCount, 
      taskMarketType, 
      taskBidPrice, 
      ec2KeyName, 
      hadoopVersion, 
      steps, 
      "", 
      jobName, 
      amiVersion, 
      loggingURI, 
      visibleToAllUsers)
    
    val job_flow_id = run_request.getJobFlowId()
    
    
    // to get cluster status 
    val ClusterState= emr.getClusterState(job_flow_id)
    
    //to get additional cluster information write access method to members of the com.amazonaws.services.elasticmapreduce.model.Cluster class (currying), for example:
    
   def getClusterName(cluster: com.amazonaws.services.elasticmapreduce.model.Cluster): String = cluster.getName()
   val clusterName= emr.getClusterDetail(jobFlowId,getClusterName)
   
   //to shutdown the cluster 
   val response_jobFlowId = emr.TerminateCluster(jobFlowId)
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

