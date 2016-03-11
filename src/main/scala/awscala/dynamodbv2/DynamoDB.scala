package awscala.dynamodbv2

import java.util

import awscala._
import com.amazonaws.services.dynamodbv2.model
import scala.collection.JavaConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }
import com.amazonaws.services.dynamodbv2.model.KeysAndAttributes

object DynamoDB {

  def apply(credentials: Credentials)(implicit region: Region): DynamoDB = new DynamoDBClient(BasicCredentialsProvider(credentials.getAWSAccessKeyId, credentials.getAWSSecretKey)).at(region)
  def apply(credentialsProvider: CredentialsProvider = CredentialsLoader.load())(implicit region: Region = Region.default()): DynamoDB = new DynamoDBClient(credentialsProvider).at(region)
  def apply(accessKeyId: String, secretAccessKey: String)(implicit region: Region): DynamoDB = new DynamoDBClient(BasicCredentialsProvider(accessKeyId, secretAccessKey)).at(region)

  def at(region: Region): DynamoDB = apply()(region)

  def local(): DynamoDB = {
    val client = DynamoDB("", "")(Region.default())
    client.setEndpoint("http://localhost:8000")
    client
  }
}

/**
 * Amazon DynamoDB Java client wrapper
 *
 * @see [[http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/]]
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
    hashPK: (String, aws.model.ScalarAttributeType)
  ): TableMeta = {
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
    otherAttributes: Seq[(String, aws.model.ScalarAttributeType)]
  ): TableMeta = {
    create(Table(
      name = name,
      hashPK = hashPK._1,
      rangePK = None,
      attributes = Seq(
      AttributeDefinition(hashPK._1, hashPK._2)
    ) ++: otherAttributes.map(a => AttributeDefinition(a._1, a._2))
    ))
  }

  def createTable(
    name: String,
    hashPK: (String, aws.model.ScalarAttributeType),
    rangePK: (String, aws.model.ScalarAttributeType),
    otherAttributes: Seq[(String, aws.model.ScalarAttributeType)],
    indexes: Seq[LocalSecondaryIndex]
  ): TableMeta = {
    create(Table(
      name = name,
      hashPK = hashPK._1,
      rangePK = Some(rangePK._1),
      attributes = Seq(
        AttributeDefinition(hashPK._1, hashPK._2),
        AttributeDefinition(rangePK._1, rangePK._2)
      ) ++: otherAttributes.map(a => AttributeDefinition(a._1, a._2)),
      localSecondaryIndexes = indexes
    ))
  }

  def create(table: Table): TableMeta = createTable(table)

  def createTable(table: Table): TableMeta = {
    val keySchema: Seq[aws.model.KeySchemaElement] = Seq(
      Some(KeySchema(table.hashPK, aws.model.KeyType.HASH)),
      table.rangePK.map(n => KeySchema(n, aws.model.KeyType.RANGE))
    ).flatten.map(_.asInstanceOf[aws.model.KeySchemaElement])

    val req = new aws.model.CreateTableRequest()
      .withTableName(table.name)
      .withAttributeDefinitions(table.attributes.map(_.asInstanceOf[aws.model.AttributeDefinition]).asJava)
      .withKeySchema(keySchema.asJava)
      .withProvisionedThroughput(
        table.provisionedThroughput.map(_.asInstanceOf[aws.model.ProvisionedThroughput]).getOrElse {
          ProvisionedThroughput(readCapacityUnits = 10, writeCapacityUnits = 10)
        }
      )

    if (!table.localSecondaryIndexes.isEmpty) {
      req.setLocalSecondaryIndexes(table.localSecondaryIndexes.map(_.asInstanceOf[aws.model.LocalSecondaryIndex]).asJava)
    }
    if (!table.globalSecondaryIndexes.isEmpty) {
      req.setGlobalSecondaryIndexes(table.globalSecondaryIndexes.map(_.asInstanceOf[aws.model.GlobalSecondaryIndex]).asJava)
    }

    TableMeta(createTable(req).getTableDescription)
  }

  def updateTableProvisionedThroughput(table: Table, provisionedThroughput: ProvisionedThroughput): TableMeta = {
    TableMeta(updateTable(
      new aws.model.UpdateTableRequest(table.name, provisionedThroughput)
    ).getTableDescription)
  }

  def delete(table: Table): Unit = deleteTable(table)
  def deleteTable(table: Table): Unit = deleteTable(table.name)

  // ------------------------------------------
  // Items
  // ------------------------------------------

  def get(table: Table, hashPK: Any): Option[Item] = getItem(table, hashPK)

  def getItem(table: Table, hashPK: Any): Option[Item] = getItem(table.name, (table.hashPK, hashPK))

  def getItem(tableName: String, hashPK: (String, Any)): Option[Item] = try {
    val attributes = getItem(new aws.model.GetItemRequest()
      .withTableName(tableName)
      .withKey(Map(hashPK._1 -> AttributeValue.toJavaValue(hashPK._2)).asJava)
      .withConsistentRead(consistentRead)).getItem
    Option(attributes).map(Item(_))
  } catch { case e: aws.model.ResourceNotFoundException => None }

  def get(table: Table, hashPK: Any, rangePK: Any): Option[Item] = getItem(table, hashPK, rangePK)

  def getItem(table: Table, hashPK: Any, rangePK: Any): Option[Item] = {
    rangePK match {
      case None => getItem(table, hashPK)
      case _ =>
        try {
          val attributes: util.Map[String, model.AttributeValue] = getItem(new aws.model.GetItemRequest()
            .withTableName(table.name)
            .withKey(Map(
              table.hashPK -> AttributeValue.toJavaValue(hashPK),
              table.rangePK.get -> AttributeValue.toJavaValue(rangePK)
            ).asJava)
            .withConsistentRead(consistentRead)).getItem

          Option(attributes).map(Item(_))
        } catch { case e: aws.model.ResourceNotFoundException => None }
    }
  }

  def batchGet(tableAndAttributes: Map[Table, List[(String, Any)]]): Seq[Item] = {
    import com.amazonaws.services.dynamodbv2.model.{ BatchGetItemRequest, BatchGetItemResult }

    case class State(items: List[Item], keys: java.util.Map[String, KeysAndAttributes])

    @scala.annotation.tailrec
    def next(state: State): (Option[Item], State) =
      state match {
        case State(head :: tail, remaining) => (Some(head), State(tail, remaining))
        case State(Nil, remaining) if !remaining.isEmpty => {
          val result = batchGetItem(new BatchGetItemRequest(remaining))
          next(State(toItems(result).toList, result.getUnprocessedKeys()))
        }
        case State(Nil, remaining) if remaining.isEmpty => (None, state)
      }

    def toStream(state: State): Stream[Item] =
      next(state) match {
        case (Some(item), nextState) => Stream.cons(item, toStream(nextState))
        case (None, _) => Stream.Empty
      }

    def toItems(result: BatchGetItemResult): Seq[Item] = {
      result.getResponses.asScala.toSeq.flatMap {
        case (t, as) => { table(t).map(table => as.asScala.toSeq.map { a => Item(a) }).getOrElse(Nil) }
      }
    }

    def toJava(tableAndAttributes: Map[Table, List[(String, Any)]]) =
      tableAndAttributes.map {
        case (table, attributes) =>
          table.name -> new KeysAndAttributes().withKeys(
            attributes.map {
            case (k, v) => Map(k -> AttributeValue.toJavaValue(v)).asJava
          }.asJava
          )
      }.asJava

    toStream(State(Nil, toJava(tableAndAttributes)))
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

  def attributeValues(attributes: Seq[(String, Any)]): java.util.Map[String, aws.model.AttributeValue] =
    attributes.toMap.mapValues(AttributeValue.toJavaValue(_)).asJava

  def put(table: Table, attributes: (String, Any)*): Unit = putItem(table.name, attributes: _*)
  def putItem(tableName: String, attributes: (String, Any)*): Unit = {
    putItem(new aws.model.PutItemRequest()
      .withTableName(tableName)
      .withItem(attributeValues(attributes)))
  }

  def putConditional(tableName: String, attributes: (String, Any)*)(cond: Seq[(String, aws.model.ExpectedAttributeValue)]): Unit = {
    putItem(new aws.model.PutItemRequest()
      .withTableName(tableName)
      .withItem(attributeValues(attributes))
      .withExpected(cond.toMap.asJava))
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

  private[dynamodbv2] def updateAttributes(
    table: Table, hashPK: Any, rangePK: Option[Any], action: AttributeAction, attributes: Seq[(String, Any)]
  ): Unit = {

    val tableKeys = Map(table.hashPK -> AttributeValue.toJavaValue(hashPK)) ++ rangePK.flatMap(rKey => table.rangePK.map(_ -> AttributeValue.toJavaValue(rKey)))

    updateItem(new aws.model.UpdateItemRequest()
      .withTableName(table.name)
      .withKey(tableKeys.asJava)
      .withAttributeUpdates(attributes.map {
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

  def deleteItem(tableName: String, hashPK: (String, Any)): Unit = {
    deleteItem(new aws.model.DeleteItemRequest()
      .withTableName(tableName)
      .withKey(Map(
        hashPK._1 -> AttributeValue.toJavaValue(hashPK._2)
      ).asJava))
  }

  def deleteItem(tableName: String, hashPK: (String, Any), rangePK: (String, Any)): Unit = {
    deleteItem(new aws.model.DeleteItemRequest()
      .withTableName(tableName)
      .withKey(Map(
        hashPK._1 -> AttributeValue.toJavaValue(hashPK._2),
        rangePK._1 -> AttributeValue.toJavaValue(rangePK._2)
      ).asJava))
  }

  def queryWithIndex(
    table: Table,
    index: SecondaryIndex,
    keyConditions: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    scanIndexForward: Boolean = true,
    consistentRead: Boolean = false
  ): Seq[Item] = try {

    val req = new aws.model.QueryRequest()
      .withTableName(table.name)
      .withIndexName(index.name)
      .withKeyConditions(keyConditions.toMap.asJava)
      .withSelect(select)
      .withScanIndexForward(scanIndexForward)
      .withConsistentRead(consistentRead)
    if (!attributesToGet.isEmpty) {
      req.setAttributesToGet(attributesToGet.asJava)
    }

    query(req).getItems.asScala.map(i => Item(i)).toSeq
  } catch { case e: aws.model.ResourceNotFoundException => Nil }

  def query(
    table: Table,
    keyConditions: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    scanIndexForward: Boolean = true,
    consistentRead: Boolean = false
  ): Seq[Item] = try {

    val req = new aws.model.QueryRequest()
      .withTableName(table.name)
      .withKeyConditions(keyConditions.toMap.asJava)
      .withSelect(select)
      .withScanIndexForward(scanIndexForward)
      .withConsistentRead(consistentRead)
    if (!attributesToGet.isEmpty) {
      req.setAttributesToGet(attributesToGet.asJava)
    }

    query(req).getItems.asScala.map(i => Item(i)).toSeq
  } catch { case e: aws.model.ResourceNotFoundException => Nil }

  def scan(
    table: Table,
    filter: Seq[(String, aws.model.Condition)],
    limit: Int = 1000,
    segment: Int = 0,
    totalSegments: Int = 1,
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil
  ): Seq[Item] = try {

    val req = new aws.model.ScanRequest()
      .withTableName(table.name)
      .withScanFilter(filter.toMap.asJava)
      .withSelect(select)
      .withLimit(limit)
      .withSegment(segment)
      .withTotalSegments(totalSegments)
    if (!attributesToGet.isEmpty) {
      req.setAttributesToGet(attributesToGet.asJava)
    }

    scan(req).getItems.asScala.map(i => Item(i)).toSeq
  } catch { case e: aws.model.ResourceNotFoundException => Nil }

}

/**
 * Default Implementation
 *
 * @param credentialsProvider credentialsProvider
 */
class DynamoDBClient(credentialsProvider: CredentialsProvider = CredentialsLoader.load())
  extends aws.AmazonDynamoDBClient(credentialsProvider)
  with DynamoDB

