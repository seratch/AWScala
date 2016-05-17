package awscala.dynamodbv2

import awscala._
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

  def getItem(table: Table, hashPK: Any): Option[Item] = try {
    val attributes = getItem(new aws.model.GetItemRequest()
      .withTableName(table.name)
      .withKey(Map(table.hashPK -> AttributeValue.toJavaValue(hashPK)).asJava)
      .withConsistentRead(consistentRead)).getItem

    Option(attributes).map(Item(table, _))
  } catch { case e: aws.model.ResourceNotFoundException => None }

  def get(table: Table, hashPK: Any, rangePK: Any): Option[Item] = getItem(table, hashPK, rangePK)

  def getItem(table: Table, hashPK: Any, rangePK: Any): Option[Item] = {
    rangePK match {
      case None => getItem(table, hashPK)
      case _ =>
        try {
          val attributes = getItem(new aws.model.GetItemRequest()
            .withTableName(table.name)
            .withKey(Map(
              table.hashPK -> AttributeValue.toJavaValue(hashPK),
              table.rangePK.get -> AttributeValue.toJavaValue(rangePK)
            ).asJava)
            .withConsistentRead(consistentRead)).getItem

          Option(attributes).map(Item(table, _))
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
        case (t, as) => { table(t).map(table => as.asScala.toSeq.map { a => Item(table, a) }).getOrElse(Nil) }
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

  def queryWithIndex(
    table: Table,
    index: SecondaryIndex,
    keyConditions: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    scanIndexForward: Boolean = true,
    consistentRead: Boolean = false,
    limit: Int = 1000
  ): Seq[Item] = try {

    val req = new aws.model.QueryRequest()
      .withTableName(table.name)
      .withIndexName(index.name)
      .withKeyConditions(keyConditions.toMap.asJava)
      .withSelect(select)
      .withScanIndexForward(scanIndexForward)
      .withConsistentRead(consistentRead)
      .withLimit(limit)
    if (!attributesToGet.isEmpty) {
      req.setAttributesToGet(attributesToGet.asJava)
    }

    val pager = new QueryResultPager(table, query(_), req)
    pager.toSeq // will return a Stream[Item]
  } catch { case e: aws.model.ResourceNotFoundException => Nil }

  def query(
    table: Table,
    keyConditions: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    scanIndexForward: Boolean = true,
    consistentRead: Boolean = false,
    limit: Int = 1000
  ): Seq[Item] = try {

    val req = new aws.model.QueryRequest()
      .withTableName(table.name)
      .withKeyConditions(keyConditions.toMap.asJava)
      .withSelect(select)
      .withScanIndexForward(scanIndexForward)
      .withConsistentRead(consistentRead)
      .withLimit(limit)
    if (!attributesToGet.isEmpty) {
      req.setAttributesToGet(attributesToGet.asJava)
    }

    val pager = new QueryResultPager(table, query(_), req)
    pager.toSeq // will return a Stream[Item]
  } catch { case e: aws.model.ResourceNotFoundException => Nil }

  def scan(
    table: Table,
    filter: Seq[(String, aws.model.Condition)],
    limit: Int = 1000,
    segment: Int = 0,
    totalSegments: Int = 1,
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    consistentRead: Boolean = false
  ): Seq[Item] = try {

    val req = new aws.model.ScanRequest()
      .withTableName(table.name)
      .withScanFilter(filter.toMap.asJava)
      .withSelect(select)
      .withLimit(limit)
      .withSegment(segment)
      .withTotalSegments(totalSegments)
      .withConsistentRead(consistentRead)
    if (!attributesToGet.isEmpty) {
      req.setAttributesToGet(attributesToGet.asJava)
    }

    val pager = new ScanResultPager(table, scan(_), req)
    pager.toSeq // will return a Stream[Item]
  } catch { case e: aws.model.ResourceNotFoundException => Nil }
}

// a pager specialized for query request/results
class QueryResultPager(val table: Table, val operation: aws.model.QueryRequest => aws.model.QueryResult, val request: aws.model.QueryRequest)
  extends ResultPager[aws.model.QueryRequest, aws.model.QueryResult] {
  override def getItems(result: aws.model.QueryResult) = result.getItems
  override def getLastEvaluatedKey(result: aws.model.QueryResult) = result.getLastEvaluatedKey
  override def withExclusiveStartKey(request: aws.model.QueryRequest, lastKey: java.util.Map[String, aws.model.AttributeValue]) = request.withExclusiveStartKey(lastKey)
}

// a pager specialized for scan request/results
class ScanResultPager(val table: Table, val operation: aws.model.ScanRequest => aws.model.ScanResult, val request: aws.model.ScanRequest)
  extends ResultPager[aws.model.ScanRequest, aws.model.ScanResult] {
  override def getItems(result: aws.model.ScanResult) = result.getItems
  override def getLastEvaluatedKey(result: aws.model.ScanResult) = result.getLastEvaluatedKey
  override def withExclusiveStartKey(request: aws.model.ScanRequest, lastKey: java.util.Map[String, aws.model.AttributeValue]) = request.withExclusiveStartKey(lastKey)
}

/**
  * The ResultPager allows iteration over the results from a DynamoDB query/scan as a single stream of items,
  * handling the necessary paging details in the background.
  *
  * DynamoDB paginates the result of query/scan operations. The data returned from a Query or Scan operation is limited
  * to 1 MB; this means that if the result set exceeds 1 MB of data, you'll need to perform another Query or Scan
  * operation to retrieve the next 1 MB of data. In addition, the limit parameter controls the number of items that you
  * want DynamoDB to process in a single request before returning results.
  * See http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/QueryAndScan.html#Pagination
  *
  * Each response from DynamoDB indicates if the processing has reached the end of the dataset, or if another request is
  * needed in order to continue scanning where the last request finished.
  *
  * When the items from a page is exhausted, the ResultPager will issue a new query/scan for the next page of results,
  * until processing reaches the end of the dataset, or the client stops iterating over the result (as the return value
  * is a Stream[Item])
  *
  * @tparam TReq
  * @tparam TRes
  */
trait ResultPager[TReq, TRes] extends Iterator[Item] {
  def table: Table
  def operation: TReq => TRes
  def request: TReq

  var items: Seq[Item] = null
  var pageNo = 0
  var lastKey: java.util.Map[String, aws.model.AttributeValue] = null
  var index = 0
  nextPage(request)

  def getItems(result: TRes): java.util.List[java.util.Map[String, aws.model.AttributeValue]]
  def getLastEvaluatedKey(result: TRes): java.util.Map[String, aws.model.AttributeValue]
  def withExclusiveStartKey(request: TReq, lastKey: java.util.Map[String, aws.model.AttributeValue]): TReq

  private def nextPage(request: TReq): Unit = {
    println(s"fetching page #$pageNo")
    pageNo += 1
    val result = operation(request)
    items = getItems(result).asScala.map(i => Item(table, i))
    lastKey = getLastEvaluatedKey(result)
    println(s"getLastEvaluatedKey is $lastKey")
    index = 0
  }

  override def next(): Item = {
    val item: Item = items(index)
    index += 1
    println(s"returning $item")
    item
  }

  override def hasNext: Boolean = {
    val res = if (index < items.size) {
      true
    } else if (lastKey == null) {
      false
    } else {
      do {
        nextPage(withExclusiveStartKey(request, lastKey))
      } while(lastKey != null && items.size == 0) // there are potentially more matching data, but this page didn't contain any
      items.size != 0
    }
    println(s"hasNext returning $res")
    res
  }
}

/**
 * Default Implementation
 *
 * @param credentialsProvider credentialsProvider
 */
class DynamoDBClient(credentialsProvider: CredentialsProvider = CredentialsLoader.load())
  extends aws.AmazonDynamoDBClient(credentialsProvider)
  with DynamoDB

