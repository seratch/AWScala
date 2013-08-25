package awscala

import awscala._, dynamodb._

import org.slf4j._
import org.scalatest._
import org.scalatest.matchers._
import com.amazonaws.services.{ dynamodbv2 => aws }

class DynamoDBSpec extends FlatSpec with ShouldMatchers {

  behavior of "DynamoDB"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "provide cool APIs for Hash PK tables" in {
    implicit val dynamoDB = DynamoDB.at(Region.Tokyo)

    val tableName = s"companies_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = ("id", AttributeType.String)
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

    // scan
    val found: Seq[Item] = companies.scan(Seq("url" -> Condition.isNotNull()))
    found.size should equal(2)

    companies.destroy()
  }

  it should "provide cool APIs for Hash/Range PK tables" in {
    implicit val dynamoDB = DynamoDB.at(Region.Tokyo)

    val tableName = s"members_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = ("id", AttributeType.Number),
      rangePK = ("company", AttributeType.String)
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

    members.put(1, "Google", "name" -> "Alice", "age" -> 23)
    members.put(2, "Google", "name" -> "Bob", "age" -> 36)
    members.put(3, "Amazon", "name" -> "Chris", "age" -> 29)

    // key conditions
    val chris: Option[Item] = members.query(Seq("id" -> Condition.eq(3))).headOption
    chris.get.attributes.find(_.name == "age").get.value.n.get should equal("29")

    // scan
    val found: Seq[Item] = members.scan(Seq("age" -> Condition.gt(25)))
    found.size should equal(2)

    members.destroy()
  }

}
