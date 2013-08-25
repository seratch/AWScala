package awscala.dynamodb

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }

object TableMeta {
  def apply(t: aws.model.TableDescription): TableMeta = new TableMeta(
    name = t.getTableName,
    sizeBytes = t.getTableSizeBytes,
    itemCount = t.getItemCount,
    status = aws.model.TableStatus.fromValue(t.getTableStatus),
    attributes = t.getAttributeDefinitions.asScala.map(a => AttributeDefinition(a)).toSeq,
    keySchema = t.getKeySchema.asScala.map(s => KeySchema(s)).toSeq,
    localSecondaryIndexes = Option(t.getLocalSecondaryIndexes).map { indexes =>
      indexes.asScala.map(i => LocalSecondaryIndexMeta(i)).toSeq
    }.getOrElse(Nil),
    provisionedThroughput = ProvisionedThroughputMeta(t.getProvisionedThroughput),
    createdAt = new DateTime(t.getCreationDateTime)
  )
}

case class TableMeta(
    name: String,
    sizeBytes: Long,
    itemCount: Long,
    status: TableStatus,
    attributes: Seq[AttributeDefinition],
    keySchema: Seq[KeySchema],
    localSecondaryIndexes: Seq[LocalSecondaryIndexMeta],
    provisionedThroughput: ProvisionedThroughputMeta,
    createdAt: DateTime) extends aws.model.TableDescription {

  def table: Table = Table(
    name = name,
    hashPK = keySchema.find(_.keyType == aws.model.KeyType.HASH).get.attributeName,
    rangePK = keySchema.find(_.keyType == aws.model.KeyType.RANGE).map(_.attributeName),
    attributes = attributes,
    localSecondaryIndexes = localSecondaryIndexes.map(e => LocalSecondaryIndex(e)),
    provisionedThroughput = Some(ProvisionedThroughput(provisionedThroughput))
  )

  setAttributeDefinitions(attributes.map(_.asInstanceOf[aws.model.AttributeDefinition]).asJava)
  setCreationDateTime(createdAt.toDate)
  setItemCount(itemCount)
  setKeySchema(keySchema.map(_.asInstanceOf[aws.model.KeySchemaElement]).asJava)
  setLocalSecondaryIndexes(localSecondaryIndexes.map(_.asInstanceOf[aws.model.LocalSecondaryIndexDescription]).asJava)
  setProvisionedThroughput(provisionedThroughput)
  setTableName(name)
  setTableSizeBytes(sizeBytes)
  setTableStatus(status)
}

object LocalSecondaryIndexMeta {
  def apply(i: aws.model.LocalSecondaryIndexDescription): LocalSecondaryIndexMeta = new LocalSecondaryIndexMeta(
    name = i.getIndexName,
    sizeBytes = i.getIndexSizeBytes,
    itemCount = i.getItemCount,
    keySchema = i.getKeySchema.asScala.map(k => KeySchema(k)).toSeq,
    projection = Projection(i.getProjection)
  )
}
case class LocalSecondaryIndexMeta(
    name: String,
    sizeBytes: Long,
    itemCount: Long,
    keySchema: Seq[KeySchema],
    projection: Projection) extends aws.model.LocalSecondaryIndexDescription {

  setIndexName(name)
  setIndexSizeBytes(sizeBytes)
  setItemCount(itemCount)
  setKeySchema(keySchema.map(_.asInstanceOf[aws.model.KeySchemaElement]).asJava)
  setProjection(projection)
}

object ProvisionedThroughputMeta {
  def apply(p: aws.model.ProvisionedThroughputDescription): ProvisionedThroughputMeta = new ProvisionedThroughputMeta(
    numberOfDecreasesToday = p.getNumberOfDecreasesToday,
    readCapacityUnits = p.getReadCapacityUnits,
    writeCapacityUnits = p.getWriteCapacityUnits,
    lastDecreasedAt = new DateTime(p.getLastDecreaseDateTime),
    lastIncreasedAt = new DateTime(p.getLastIncreaseDateTime)
  )
}
case class ProvisionedThroughputMeta(
    numberOfDecreasesToday: Long,
    readCapacityUnits: Long,
    writeCapacityUnits: Long,
    lastDecreasedAt: DateTime,
    lastIncreasedAt: DateTime) extends aws.model.ProvisionedThroughputDescription {

  setLastDecreaseDateTime(lastDecreasedAt.toDate)
  setLastIncreaseDateTime(lastIncreasedAt.toDate)
  setNumberOfDecreasesToday(numberOfDecreasesToday)
  setReadCapacityUnits(readCapacityUnits)
  setWriteCapacityUnits(writeCapacityUnits)
}