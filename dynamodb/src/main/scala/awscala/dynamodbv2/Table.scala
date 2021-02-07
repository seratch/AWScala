package awscala.dynamodbv2

import com.amazonaws.services.{dynamodbv2 => aws}

import scala.reflect.ClassTag

object Table {
  def apply(
    name: String,
    hashPK: String,
    rangePK: Option[String],
    attributes: Seq[AttributeDefinition],
    localSecondaryIndexes: Seq[LocalSecondaryIndex],
    globalSecondaryIndexes: Seq[GlobalSecondaryIndex],
    provisionedThroughput: Option[ProvisionedThroughput],
    billingMode: aws.model.BillingMode): Table =
    new Table(name, hashPK, rangePK, attributes, localSecondaryIndexes,
      globalSecondaryIndexes, provisionedThroughput, Option(billingMode))
}

case class Table(
  name: String,
  hashPK: String,
  rangePK: Option[String] = None,
  attributes: Seq[AttributeDefinition] = Nil,
  localSecondaryIndexes: Seq[LocalSecondaryIndex] = Nil,
  globalSecondaryIndexes: Seq[GlobalSecondaryIndex] = Nil,
  provisionedThroughput: Option[ProvisionedThroughput] = None,
  billingMode: Option[aws.model.BillingMode] = None) {

  // ------------------------------------------
  // Items
  // ------------------------------------------

  def get(hashPK: Any)(implicit dynamoDB: DynamoDB): Option[Item] = getItem(hashPK)
  def get(hashPK: Any, rangePK: Any)(implicit dynamoDB: DynamoDB): Option[Item] = getItem(hashPK, rangePK)

  def getItem(hashPK: Any)(implicit dynamoDB: DynamoDB): Option[Item] = {
    dynamoDB.get(this, hashPK)
  }
  def getItem(hashPK: Any, rangePK: Any)(
    implicit
    dynamoDB: DynamoDB): Option[Item] = {
    dynamoDB.get(this, hashPK, rangePK)
  }

  def batchGet(attributes: List[(String, Any)])(implicit dynamoDB: DynamoDB): Seq[Item] = batchGetItems(attributes)

  def batchGetItems(attributes: List[(String, Any)])(implicit dynamoDB: DynamoDB): Seq[Item] = {
    dynamoDB.batchGet(Map(this -> attributes))
  }

  def put(hashPK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB): Unit = putItem(hashPK, attributes: _*)
  def put(hashPK: Any, rangePK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB): Unit = putItem(hashPK, rangePK, attributes: _*)

  def put[E <: AnyRef](hashPK: Any, entity: E)(implicit dynamoDB: DynamoDB) = {
    dynamoDB.put(this, hashPK, getAttrValues(entity): _*)
  }

  def getAttrValues(entity: AnyRef) = {
    val fields = entity.getClass.getDeclaredFields.map(_.getName).toList
    println(fields)
    fields.map(f => f -> getValueFromEntity(f, entity))
  }

  def getValueFromEntity(field: String,entity: AnyRef) = {
    println(field)
    val fv = entity.getClass.getDeclaredField(field)
    fv.setAccessible(true)
    fv.get(entity)
  }

  def putItem(hashPK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.put(this, hashPK, attributes: _*)
  }
  def putItem(hashPK: Any, rangePK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.put(this, hashPK, rangePK, attributes: _*)
  }

  def delete(hashPK: Any)(implicit dynamoDB: DynamoDB): Unit = deleteItem(hashPK)
  def delete(hashPK: Any, rangePK: Any)(implicit dynamoDB: DynamoDB): Unit = deleteItem(hashPK, rangePK)

  def deleteItem(hashPK: Any)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.deleteItem(this, hashPK)
  }
  def deleteItem(hashPK: Any, rangePK: Any)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.deleteItem(this, hashPK, rangePK)
  }

  def queryWithIndex(
    index: SecondaryIndex,
    keyConditions: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    scanIndexForward: Boolean = true,
    consistentRead: Boolean = false,
    limit: Int = 1000,
    pageStatsCallback: PageStats => Unit = null)(implicit dynamoDB: DynamoDB): Seq[Item] = {
    dynamoDB.queryWithIndex(
      table = this,
      index = index,
      keyConditions = keyConditions,
      select = select,
      attributesToGet = attributesToGet,
      scanIndexForward = scanIndexForward,
      consistentRead = consistentRead,
      limit = limit,
      pageStatsCallback = pageStatsCallback)
  }

  def query(
    keyConditions: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    scanIndexForward: Boolean = true,
    consistentRead: Boolean = false,
    limit: Int = 1000,
    pageStatsCallback: PageStats => Unit = null)(implicit dynamoDB: DynamoDB): Seq[Item] = {
    dynamoDB.query(
      table = this,
      keyConditions = keyConditions,
      select = select,
      attributesToGet = attributesToGet,
      scanIndexForward = scanIndexForward,
      consistentRead = consistentRead,
      limit = limit,
      pageStatsCallback = pageStatsCallback)
  }

  def scan(
    filter: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    limit: Int = 1000,
    segment: Int = 0,
    totalSegments: Int = 1,
    consistentRead: Boolean = false,
    pageStatsCallback: PageStats => Unit = null)(implicit dynamoDB: DynamoDB): Seq[Item] = {
    dynamoDB.scan(
      table = this,
      filter = filter,
      limit = limit,
      segment = segment,
      totalSegments = totalSegments,
      select = select,
      attributesToGet = attributesToGet,
      consistentRead = consistentRead,
      pageStatsCallback = pageStatsCallback)
  }

  def addAttributes(hashPK: Any, attributes: (String, Any)*)(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, None, aws.model.AttributeAction.ADD, attributes)
  }
  def addAttributes(hashPK: Any, rangePK: Any, attributes: Seq[(String, Any)])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, Some(rangePK), aws.model.AttributeAction.ADD, attributes)
  }

  def deleteAttributes(hashPK: Any, attributes: Seq[(String, Any)])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, None, aws.model.AttributeAction.DELETE, attributes)
  }
  def deleteAttributes(hashPK: Any, rangePK: Any, attributes: Seq[(String, Any)])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, Some(rangePK), aws.model.AttributeAction.DELETE, attributes)
  }

  def putAttributes(hashPK: Any, attributes: Seq[(String, Any)])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, None, aws.model.AttributeAction.PUT, attributes)
  }
  def putAttributes(hashPK: Any, rangePK: Any, attributes: Seq[(String, Any)])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, Some(rangePK), aws.model.AttributeAction.PUT, attributes)
  }

  def update(throughput: ProvisionedThroughput)(implicit dynamoDB: DynamoDB): TableMeta = {
    dynamoDB.updateTableProvisionedThroughput(this, throughput)
  }

  def destroy()(implicit dynamoDB: DynamoDB): Unit = dynamoDB.delete(this)

}
