package awscala

import java.util.Date

import awscala.dynamodbv2._
import com.amazonaws.services.dynamodbv2.model.{ ProvisionedThroughputDescription, TableDescription, TableStatus }
import com.amazonaws.services.dynamodbv2.util.TableUtils
import com.amazonaws.services.{ dynamodbv2 => aws }
import org.scalatest._
import org.slf4j._

import scala.util.Try
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
class DynamoDBV2AnnotationsSpec extends AnyFlatSpec with Matchers {

  behavior of "DynamoDB"

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  case class TestMember(
    @hashPK val id: Int,
    @rangePK val Country: String,
    val Company: String,
    val Name: String,
    val Age: Int)
  it should "allows using case class with annotation in put method" in {
    implicit val dynamoDB: DynamoDB = DynamoDB.local()
    val tableName = s"Members_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Id" -> AttributeType.Number,
      rangePK = "Country" -> AttributeType.String,
      otherAttributes = Seq(
        "Company" -> AttributeType.String),
      indexes = Seq(
        LocalSecondaryIndex(
          name = "CompanyIndex",
          keySchema = Seq(KeySchema("Id", KeyType.Hash), KeySchema("Company", KeyType.Range)),
          projection = Projection(ProjectionType.Include, Seq("Company")))))
    log.info(s"Created Table: $createdTableMeta")

    println(s"Waiting for DynamoDB table activation...")
    TableUtils.waitUntilActive(dynamoDB, createdTableMeta.name)
    println("")
    println(s"Created DynamoDB table has been activated.")

    val members: Table = dynamoDB.table(tableName).get
    val member = TestMember(1, "PL", "DataMass", "Alex", 29)
    println(members)
    members.putItem(member)

    println(members)
    println(members.get(1, "PL"))
    println(members.get(1, "PL").get.attributes.find(_.name == "Name").get.value)

    members.get(1, "PL").get.attributes.find(_.name == "Name").get.value.s.get should equal("Alex")
    members.get(1, "PL").get.attributes.find(_.name == "Company").get.value.s.get should equal("DataMass")
    members.get(1, "PL").get.attributes.find(_.name == "Country").get.value.s.get should equal("PL")
    members.destroy()
  }
}
