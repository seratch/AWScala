package awscala.dynamodbv2

import com.amazonaws.services.{ dynamodbv2 => aws }

case class Table(
    name: String,
    hashPK: String,
    rangePK: Option[String] = None,
    attributes: Seq[AttributeDefinition] = Nil,
    localSecondaryIndexes: Seq[LocalSecondaryIndex] = Nil,
    provisionedThroughput: Option[ProvisionedThroughput] = None) {

  // ------------------------------------------
  // Items
  // ------------------------------------------

  def get(hashPK: Any)(implicit dynamoDB: DynamoDB): Option[Item] = getItem(hashPK)
  def get(hashPK: Any, rangePK: Any)(implicit dynamoDB: DynamoDB): Option[Item] = getItem(hashPK, rangePK)

  def getItem(hashPK: Any)(implicit dynamoDB: DynamoDB): Option[Item] = {
    dynamoDB.get(this, hashPK)
  }
  def getItem(hashPK: Any, rangePK: Any)(
    implicit dynamoDB: DynamoDB): Option[Item] = {
    dynamoDB.get(this, hashPK, rangePK)
  }

  def put(hashPK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB) = putItem(hashPK, attributes: _*)
  def put(hashPK: Any, rangePK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB) = putItem(hashPK, rangePK, attributes: _*)

  def putItem(hashPK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB) = {
    dynamoDB.put(this, hashPK, attributes: _*)
  }
  def putItem(hashPK: Any, rangePK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB) = {
    dynamoDB.put(this, hashPK, rangePK, attributes: _*)
  }

  def delete(hashPK: Any)(implicit dynamoDB: DynamoDB) = deleteItem(hashPK)
  def delete(hashPK: Any, rangePK: Any)(implicit dynamoDB: DynamoDB) = deleteItem(hashPK, rangePK)

  def deleteItem(hashPK: Any)(implicit dynamoDB: DynamoDB) = {
    dynamoDB.deleteItem(this, hashPK)
  }
  def deleteItem(hashPK: Any, rangePK: Any)(implicit dynamoDB: DynamoDB) = {
    dynamoDB.deleteItem(this, hashPK, Some(rangePK))
  }

  def queryWithIndex(
    index: LocalSecondaryIndex,
    keyConditions: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    consistentRead: Boolean = false)(implicit dynamoDB: DynamoDB): Seq[Item] = {
    dynamoDB.queryWithIndex(
      table = this,
      index = index,
      keyConditions = keyConditions,
      select = select,
      attributesToGet = attributesToGet,
      consistentRead = consistentRead)
  }

  def query(
    keyConditions: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    consistentRead: Boolean = false)(implicit dynamoDB: DynamoDB): Seq[Item] = {
    dynamoDB.query(
      table = this,
      keyConditions = keyConditions,
      select = select,
      attributesToGet = attributesToGet,
      consistentRead = consistentRead)
  }

  def scan(filter: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    limit: Int = 1000,
    segment: Int = 0,
    totalSegments: Int = 1)(implicit dynamoDB: DynamoDB): Seq[Item] = {
    dynamoDB.scan(
      table = this,
      filter = filter,
      limit = limit,
      segment = segment,
      totalSegments = totalSegments,
      select = select,
      attributesToGet = attributesToGet)
  }

  def addAttributes(hashPK: Any, attributes: (String, Any)*)(
    implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, None, aws.model.AttributeAction.ADD, attributes)
  }
  def addAttributes(hashPK: Any, rangePK: Any, attributes: Seq[(String, Any)])(
    implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, Some(rangePK), aws.model.AttributeAction.ADD, attributes)
  }

  def deleteAttributes(hashPK: Any, attributes: Seq[(String, Any)])(
    implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, None, aws.model.AttributeAction.DELETE, attributes)
  }
  def deleteAttributes(hashPK: Any, rangePK: Any, attributes: Seq[(String, Any)])(
    implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, Some(rangePK), aws.model.AttributeAction.DELETE, attributes)
  }

  def putAttributes(hashPK: Any, attributes: Seq[(String, Any)])(
    implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, None, aws.model.AttributeAction.PUT, attributes)
  }
  def putAttributes(hashPK: Any, rangePK: Any, attributes: Seq[(String, Any)])(
    implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, Some(rangePK), aws.model.AttributeAction.PUT, attributes)
  }

  def update(throughput: ProvisionedThroughput)(implicit dynamoDB: DynamoDB) = {
    dynamoDB.updateTableProvisionedThroughput(this, throughput)
  }

  def destroy()(implicit dynamoDB: DynamoDB) = dynamoDB.delete(this)

}
