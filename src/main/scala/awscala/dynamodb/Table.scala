package awscala.dynamodb

import com.amazonaws.services.{ dynamodbv2 => aws }

case class Table(
    name: String,
    hashPK: String,
    rangePK: Option[String] = None,
    attributes: Seq[AttributeDefinition] = Nil,
    localSecondaryIndexes: Seq[LocalSecondaryIndex] = Nil,
    provisionedThroughput: Option[ProvisionedThroughput] = None) {

  def getItem(hashPK: Any)(
    implicit dynamoDB: DynamoDB): Option[Item] = {
    dynamoDB.get(this, hashPK, None)
  }
  def getItem(hashPK: Any, rangePK: Any)(
    implicit dynamoDB: DynamoDB): Option[Item] = {
    dynamoDB.get(this, hashPK, rangePK)
  }

  def putItem(hashPK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB) = {
    dynamoDB.put(this, hashPK, attributes: _*)
  }
  def putItem(hashPK: Any, rangePK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB) = {
    dynamoDB.put(this, hashPK, rangePK, attributes: _*)
  }

  def deleteItem(hashPK: Any)(implicit dynamoDB: DynamoDB) = {
    dynamoDB.deleteItem(this, hashPK, None)
  }
  def deleteItem(hashPK: Any, rangePK: Any)(implicit dynamoDB: DynamoDB) = {
    dynamoDB.deleteItem(this, hashPK, Some(rangePK))
  }

  def query(keyConditions: Seq[(String, Condition)],
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

  def scan(filter: Seq[(String, Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil)(implicit dynamoDB: DynamoDB): Seq[Item] = {
    dynamoDB.scan(
      table = this,
      filter = filter,
      select = select,
      attributesToGet = attributesToGet)
  }

  def addAttributes(table: Table, hashPK: Any, rangePK: Option[Any] = None, attributes: Seq[(String, Any)])(
    implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(table, hashPK, rangePK, aws.model.AttributeAction.ADD, attributes)
  }
  def deleteAttributes(table: Table, hashPK: Any, rangePK: Option[Any] = None, attributes: Seq[(String, Any)])(
    implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(table, hashPK, rangePK, aws.model.AttributeAction.DELETE, attributes)
  }
  def putAttributes(table: Table, hashPK: Any, rangePK: Option[Any] = None, attributes: Seq[(String, Any)])(
    implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(table, hashPK, rangePK, aws.model.AttributeAction.PUT, attributes)
  }

  def update(throughput: ProvisionedThroughput)(implicit dynamoDB: DynamoDB) = {
    dynamoDB.updateTableProvisionedThroughput(this, throughput)
  }
  def destroy()(implicit dynamoDB: DynamoDB) = dynamoDB.delete(this)
}
