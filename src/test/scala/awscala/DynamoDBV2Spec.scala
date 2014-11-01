package awscala

import awscala.dynamodbv2._
import com.amazonaws.services.{ dynamodbv2 => aws }
import org.scalatest._
import org.slf4j._

class DynamoDBV2Spec extends FlatSpec with Matchers {

  behavior of "DynamoDB"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "provide cool APIs for Hash PK tables" in {
    implicit val dynamoDB = DynamoDB.local()

    val tableName = s"Companies_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Id" -> AttributeType.String
    )
    log.info(s"Created Table: ${createdTableMeta}")

    println(s"Waiting for DynamoDB table activation...")
    var isTableActivated = false
    while (!isTableActivated) {
      dynamoDB.describe(createdTableMeta.table).map { meta =>
        isTableActivated = meta.status == aws.model.TableStatus.ACTIVE
      }
      Thread.sleep(1000L)
      print(".")
    }
    println("")
    println(s"Created DynamoDB table has been activated.")

    val companies: Table = dynamoDB.table(tableName).get

    companies.put("Amazon", "url" -> "http://www.amazon.com/")
    companies.put("Google", "url" -> "http://www.google.com/")
    companies.put("Microsoft")

    // get by primary key
    val google: Option[Item] = companies.get("Google")
    google.get.attributes.find(_.name == "url").get.value.s.get should equal("http://www.google.com/")

    val nonExistant: Option[Item] = companies.get("I Don't Exist")
    nonExistant.isDefined should not be true

    // batch get
    val batchedCompanies: Seq[Item] = companies.batchGet(List(("Id", "Google"), ("Id", "Microsoft")))
    batchedCompanies.size should equal(2)
    batchedCompanies.map(item => item.attributes.find(_.name == "Id").get.value.s.get.equals("Google")
      || item.attributes.find(_.name == "Id").get.value.s.get.equals("Microsoft")) should equal(Seq(true, true))

    val batchedNonExistant: Seq[Item] = companies.batchGet(List(("Id", "I Don't Exist"), ("Id", "Neither Do I")))
    batchedNonExistant.size should equal(0)

    // scan
    val foundCompanies: Seq[Item] = companies.scan(Seq("url" -> Condition.isNotNull))
    foundCompanies.size should equal(2)

    val scanNonExistant: Seq[Item] = companies.scan(Seq("url" -> Condition.eq("I Don't Exist")))
    scanNonExistant.size should equal(0)

    // putAttributes
    companies.putAttributes("Microsoft", Seq("url" -> "http://www.microsoft.com"))
    companies.get("Microsoft").get.attributes.find(_.name == "url").get.value.s.get should equal("http://www.microsoft.com")

    companies.destroy()
  }

  it should "provide cool APIs for Hash/Range PK tables" in {
    implicit val dynamoDB = DynamoDB.local()

    val tableName = s"Members_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Id" -> AttributeType.Number,
      rangePK = "Country" -> AttributeType.String,
      otherAttributes = Seq("Company" -> AttributeType.String),
      indexes = Seq(
        LocalSecondaryIndex(
          name = "CompanyIndex",
          keySchema = Seq(KeySchema("Id", KeyType.Hash), KeySchema("Company", KeyType.Range)),
          projection = Projection(ProjectionType.Include, Seq("Company"))
        )
      )
    )
    log.info(s"Created Table: ${createdTableMeta}")

    println(s"Waiting for DynamoDB table activation...")
    var isTableActivated = false
    while (!isTableActivated) {
      dynamoDB.describe(createdTableMeta.table).map { meta =>
        isTableActivated = meta.status == aws.model.TableStatus.ACTIVE
      }
      Thread.sleep(1000L)
      print(".")
    }
    println("")
    println(s"Created DynamoDB table has been activated.")

    val members: Table = dynamoDB.table(tableName).get

    members.put(1, "Japan", "Name" -> "Alice", "Age" -> 23, "Company" -> "Google")
    members.put(2, "U.S.", "Name" -> "Bob", "Age" -> 36, "Company" -> "Google")
    members.put(3, "Japan", "Name" -> "Chris", "Age" -> 29, "Company" -> "Amazon")

    val nonExistant: Option[Item] = members.get(4, "U.K.")
    nonExistant.isDefined should not be true

    val googlers: Seq[Item] = members.scan(Seq("Company" -> Condition.eq("Google")))
    googlers.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should equal(Seq("Bob", "Alice"))

    val scanNonExistant: Seq[Item] = members.scan(Seq("Company" -> Condition.eq("I Don't Exist")))
    scanNonExistant.size should equal(0)

    // putAttributes
    members.putAttributes(3, "Japan", Seq("Company" -> "Microsoft"))
    members.get(3, "Japan").get.attributes.find(_.name == "Company").get.value.s.get should equal("Microsoft")

    members.destroy()
  }

  it should "provide cool APIs to use global secondary index" in {
    implicit val dynamoDB = DynamoDB.local()

    val tableName = s"Users_${System.currentTimeMillis}"
    val globalSecondaryIndex = GlobalSecondaryIndex(
      name = "SexIndex",
      keySchema = Seq(KeySchema("Sex", KeyType.Hash), KeySchema("Age", KeyType.Range)),
      projection = Projection(ProjectionType.All),
      provisionedThroughput = ProvisionedThroughput(readCapacityUnits = 10, writeCapacityUnits = 10)
    )
    val table = Table(
      name = tableName,
      hashPK = "Id",
      attributes = Seq(
        AttributeDefinition("Id", AttributeType.Number),
        AttributeDefinition("Sex", AttributeType.String),
        AttributeDefinition("Age", AttributeType.Number)
      ),
      globalSecondaryIndexes = Seq(globalSecondaryIndex)
    )
    val createdTableMeta: TableMeta = dynamoDB.createTable(table)
    log.info(s"Created Table: ${createdTableMeta}")

    println(s"Waiting for DynamoDB table activation...")
    var isTableActivated = false
    while (!isTableActivated) {
      dynamoDB.describe(createdTableMeta.table).map { meta =>
        isTableActivated = meta.status == aws.model.TableStatus.ACTIVE
      }
      Thread.sleep(1000L)
      print(".")
    }
    println("")
    println(s"Created DynamoDB table has been activated.")

    val users: Table = dynamoDB.table(tableName).get

    users.put(1, "Name" -> "John", "Sex" -> "Male", "Age" -> 12)
    users.put(2, "Name" -> "Bob", "Sex" -> "Male", "Age" -> 14)
    users.put(3, "Name" -> "Chris", "Sex" -> "Female", "Age" -> 9)
    users.put(4, "Name" -> "Michael", "Sex" -> "Male", "Age" -> 65)

    val teenageBoys: Seq[Item] = users.queryWithIndex(
      index = globalSecondaryIndex,
      keyConditions = Seq("Sex" -> Condition.eq("Male"), "Age" -> Condition.lt(20))
    )

    teenageBoys.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should equal(Seq("John", "Bob"))

    users.destroy()
  }

}
