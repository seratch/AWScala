package awscala

import awscala._, dynamodbv2._

import org.slf4j._
import org.scalatest._
import org.scalatest.matchers._
import com.amazonaws.services.{ dynamodbv2 => aws }

class DynamoDBV2Spec extends FlatSpec with ShouldMatchers {

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

    // batch get
    val batchedCompanies: Seq[Item] = companies.batchGet(List(("Id", "Google"), ("Id", "Microsoft")))
    batchedCompanies.size should equal(2)
    batchedCompanies.map(item => item.attributes.find(_.name == "Id").get.value.s.get.equals("Google")
      || item.attributes.find(_.name == "Id").get.value.s.get.equals("Microsoft")) should equal(Seq(true, true))

    // scan
    val foundCompanies: Seq[Item] = companies.scan(Seq("url" -> Condition.isNotNull))
    foundCompanies.size should equal(2)

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

    val googlers: Seq[Item] = members.scan(Seq("Company" -> Condition.eq("Google")))
    googlers.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should equal(Seq("Bob", "Alice"))

    members.destroy()
  }

}
