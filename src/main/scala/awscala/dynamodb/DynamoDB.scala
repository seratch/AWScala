package awscala.dynamodb

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }

object DynamoDB {

  def apply(credentials: Credentials = Credentials.defaultEnv): DynamoDB = new DynamoDBClient(credentials)
  def apply(accessKeyId: String, secretAccessKey: String): DynamoDB = apply(Credentials(accessKeyId, secretAccessKey))

  def at(region: Region): DynamoDB = apply().at(region)
}

/**
 * Amazon DynamoDB Java client wrapper
 * @see "http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/"
 */
trait DynamoDB extends aws.AmazonDynamoDB {

  def at(region: Region): DynamoDB = {
    this.setRegion(region)
    this
  }

  private[this] var consistentRead = false

  def consistentRead(consistentRead: Boolean): DynamoDB = {
    this.consistentRead = consistentRead
    this
  }

  // ------------------------------------------
  // Tables
  // ------------------------------------------

  def tableNames: Seq[String] = listTables.getTableNames.asScala
  def lastEvaluatedTableName: Option[String] = Option(listTables.getLastEvaluatedTableName)

  def describe(table: Table): Option[TableMeta] = describe(table.name)
  def describe(tableName: String): Option[TableMeta] = try {
    Option(TableMeta(describeTable(new aws.model.DescribeTableRequest().withTableName(tableName)).getTable))
  } catch { case e: aws.model.ResourceNotFoundException => None }

  def table(name: String): Option[Table] = describe(name).map(_.table)

  def createTable(
    name: String,
    hashPK: (String, aws.model.ScalarAttributeType)): TableMeta = {
    create(Table(
      name = name,
      hashPK = hashPK._1,
      rangePK = None,
      attributes = Seq(AttributeDefinition(hashPK._1, hashPK._2))
    ))
  }

  def createTable(
    name: String,
    hashPK: (String, aws.model.ScalarAttributeType),
    rangePK: (String, aws.model.ScalarAttributeType)): TableMeta = {
    create(Table(
      name = name,
      hashPK = hashPK._1,
      rangePK = Some(rangePK._1),
      attributes = Seq(
        AttributeDefinition(hashPK._1, hashPK._2),
        AttributeDefinition(rangePK._1, rangePK._2)
      )
    ))
  }

  def create(table: Table): TableMeta = createTable(table)
  def createTable(table: Table): TableMeta = {
    val keySchema: Seq[aws.model.KeySchemaElement] = Seq(
      Some(KeySchema(table.hashPK, aws.model.KeyType.HASH)),
      table.rangePK.map(n => KeySchema(n, aws.model.KeyType.RANGE))
    ).flatten.map(_.asInstanceOf[aws.model.KeySchemaElement])

    val req = new aws.model.CreateTableRequest()
      .withAttributeDefinitions(table.attributes.map(_.asInstanceOf[aws.model.AttributeDefinition]).asJava)
      .withKeySchema(keySchema.asJava)
      .withProvisionedThroughput(
        table.provisionedThroughput.map(_.asInstanceOf[aws.model.ProvisionedThroughput]).getOrElse {
          ProvisionedThroughput(readCapacityUnits = 10, writeCapacityUnits = 10)
        }
      ).withTableName(table.name)
    if (!table.localSecondaryIndexes.isEmpty) {
      req.setLocalSecondaryIndexes(table.localSecondaryIndexes.map(_.asInstanceOf[aws.model.LocalSecondaryIndex]).asJava)
    }

    TableMeta(createTable(req).getTableDescription)
  }

  def updateTableProvisionedThroughput(table: Table, provisionedThroughput: ProvisionedThroughput): TableMeta = {
    TableMeta(updateTable(
      new aws.model.UpdateTableRequest().withProvisionedThroughput(provisionedThroughput)).getTableDescription)
  }

  def delete(table: Table): Unit = deleteTable(table)
  def deleteTable(table: Table): Unit = deleteTable(table.name)
  def deleteTable(tableName: String): Unit = deleteTable(new aws.model.DeleteTableRequest().withTableName(tableName))

  // ------------------------------------------
  // Items
  // ------------------------------------------

  def get(table: Table, hashPK: Any): Option[Item] = getItem(table, hashPK)

  def getItem(table: Table, hashPK: Any): Option[Item] = try {
    Some(Item(table, getItem(new aws.model.GetItemRequest()
      .withTableName(table.name)
      .withKey(Map(table.hashPK -> AttributeValue.toJavaValue(hashPK)).asJava)
      .withConsistentRead(consistentRead)
    ).getItem))
  } catch { case e: aws.model.ResourceNotFoundException => None }

  def get(table: Table, hashPK: Any, rangePK: Any): Option[Item] = getItem(table, hashPK, rangePK)

  def getItem(table: Table, hashPK: Any, rangePK: Any): Option[Item] = {
    rangePK match {
      case None => getItem(table, hashPK)
      case _ =>
        try {
          Some(Item(table, getItem(new aws.model.GetItemRequest()
            .withTableName(table.name)
            .withKey(Map(
              table.hashPK -> AttributeValue.toJavaValue(hashPK),
              table.rangePK.get -> AttributeValue.toJavaValue(rangePK)
            ).asJava)
            .withConsistentRead(consistentRead)
          ).getItem))
        } catch { case e: aws.model.ResourceNotFoundException => None }
    }
  }

  def put(table: Table, hashPK: Any, attributes: (String, Any)*): Unit = {
    putItem(table, hashPK, attributes: _*)
  }
  def putItem(table: Table, hashPK: Any, attributes: (String, Any)*): Unit = {
    put(table, Seq(table.hashPK -> hashPK) ++: attributes: _*)
  }

  def put(table: Table, hashPK: Any, rangePK: Any, attributes: (String, Any)*): Unit = {
    putItem(table, hashPK, rangePK, attributes: _*)
  }
  def putItem(table: Table, hashPK: Any, rangePK: Any, attributes: (String, Any)*): Unit = {
    put(table, Seq(table.hashPK -> hashPK, table.rangePK.get -> rangePK) ++: attributes: _*)
  }

  def put(table: Table, attributes: (String, Any)*): Unit = putItem(table.name, attributes: _*)
  def putItem(tableName: String, attributes: (String, Any)*): Unit = {
    val values: Map[String, aws.model.AttributeValue] = attributes.map {
      case (key, value) =>
        (key, AttributeValue.toJavaValue(value))
    }.toMap
    putItem(new aws.model.PutItemRequest().withTableName(tableName).withItem(values.asJava))
  }

  def addAttributes(table: Table, hashPK: Any, attributes: (String, Any)*): Unit = {
    updateAttributes(table, hashPK, None, aws.model.AttributeAction.ADD, attributes)
  }
  def addAttributes(table: Table, hashPK: Any, rangePK: Any, attributes: (String, Any)*): Unit = {
    updateAttributes(table, hashPK, Some(rangePK), aws.model.AttributeAction.ADD, attributes)
  }

  def deleteAttributes(table: Table, hashPK: Any, attributes: (String, Any)*): Unit = {
    updateAttributes(table, hashPK, None, aws.model.AttributeAction.DELETE, attributes)
  }
  def deleteAttributes(table: Table, hashPK: Any, rangePK: Any, attributes: (String, Any)*): Unit = {
    updateAttributes(table, hashPK, Some(rangePK), aws.model.AttributeAction.DELETE, attributes)
  }

  def putAttributes(table: Table, hashPK: Any, attributes: (String, Any)*): Unit = {
    updateAttributes(table, hashPK, None, aws.model.AttributeAction.PUT, attributes)
  }
  def putAttributes(table: Table, hashPK: Any, rangePK: Any, attributes: (String, Any)*): Unit = {
    updateAttributes(table, hashPK, Some(rangePK), aws.model.AttributeAction.PUT, attributes)
  }

  private[dynamodb] def updateAttributes(table: Table, hashPK: Any, rangePK: Option[Any], action: AttributeAction, attributes: Seq[(String, Any)]): Unit = {
    updateItem(new aws.model.UpdateItemRequest().withAttributeUpdates(attributes.map {
      case (key, value) =>
        (key, new aws.model.AttributeValueUpdate().withAction(action).withValue(AttributeValue.toJavaValue(value)))
    }.toMap.asJava))
  }

  def deleteItem(table: Table, hashPK: Any): Unit = {
    deleteItem(new aws.model.DeleteItemRequest()
      .withTableName(table.name)
      .withKey(Map(table.hashPK -> AttributeValue.toJavaValue(hashPK)).asJava))
  }
  def deleteItem(table: Table, hashPK: Any, rangePK: Any): Unit = {
    deleteItem(new aws.model.DeleteItemRequest()
      .withTableName(table.name)
      .withKey(Map(
        table.hashPK -> AttributeValue.toJavaValue(hashPK),
        table.rangePK.get -> AttributeValue.toJavaValue(rangePK)
      ).asJava))
  }

  def query(table: Table,
    keyConditions: Seq[(String, Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    consistentRead: Boolean = false): Seq[Item] = try {

    val req = new aws.model.QueryRequest()
      .withTableName(table.name)
      .withKeyConditions(keyConditions.toMap.asJava)
      .withSelect(select)
      .withConsistentRead(consistentRead)
    if (!attributesToGet.isEmpty) {
      req.setAttributesToGet(attributesToGet.asJava)
    }

    query(req).getItems.asScala.map(i => Item(table, i)).toSeq
  } catch { case e: aws.model.ResourceNotFoundException => Nil }

  def scan(table: Table,
    filter: Seq[(String, Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil): Seq[Item] = try {

    val req = new aws.model.ScanRequest()
      .withTableName(table.name)
      .withScanFilter(filter.toMap.asJava)
      .withSelect(select)
    if (!attributesToGet.isEmpty) {
      req.setAttributesToGet(attributesToGet.asJava)
    }

    scan(req).getItems.asScala.map(i => Item(table, i)).toSeq
  } catch { case e: aws.model.ResourceNotFoundException => Nil }

}

/**
 * Default Implementation
 *
 * @param credentials credentials
 */
class DynamoDBClient(credentials: Credentials = Credentials.defaultEnv)
  extends aws.AmazonDynamoDBClient(credentials)
  with DynamoDB

