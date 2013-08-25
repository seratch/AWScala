package awscala

import awscala._, dynamodb._

import org.slf4j._
import org.scalatest._
import org.scalatest.matchers._
import com.amazonaws.services.{ dynamodbv2 => aws }

class DynamoDBSpec extends FlatSpec with ShouldMatchers {

  behavior of "DynamoDB"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "provide cool APIs" in {
    implicit val dynamoDB = DynamoDB.at(Region.Tokyo)

    val tableName = s"members_${System.currentTimeMillis}"
    val tableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = ("id", AttributeType.Number),
      rangePK = ("company", AttributeType.String)
    )
    log.info(s"Created Domain: ${tableMeta}")

    println(s"Waiting for DynamoDB table activation...")
    var isTableActivated = false
    while (!isTableActivated) {
      dynamoDB.describe(tableMeta.table).map { meta =>
        isTableActivated = meta.status == aws.model.TableStatus.ACTIVE
      }
      Thread.sleep(1000L)
      print(".")
    }
    println("")
    println(s"Created DynamoDB table has been activated.")

    val table: Table = dynamoDB.table(tableName).get

    table.putItem(1, "Typesafe", "name" -> "Alice", "age" -> 23)
    table.putItem(2, "Typesafe", "name" -> "Bob", "age" -> 36)
    table.putItem(3, "Oracle", "name" -> "Chris", "age" -> 29)

    // key conditions
    val foundByKey: Seq[Item] = table.query(Seq("id" -> Condition.eq(2)))

    foundByKey.size should equal(1)
    foundByKey.head.attributes.find(_.name == "age").get.value.n.get should equal("36")

    // scan
    val items: Seq[Item] = table.scan(Seq("age" -> Condition.gt(25)))
    items.size should equal(2)

    table.destroy()
  }

}
