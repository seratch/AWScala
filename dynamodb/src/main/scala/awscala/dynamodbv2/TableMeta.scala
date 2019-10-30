package awscala.dynamodbv2

import awscala._
import com.amazonaws.services.{ dynamodbv2 => aws }

import scala.jdk.CollectionConverters._

object TableMeta {
  def apply(t: aws.model.TableDescription): TableMeta = new TableMeta(
    name = t.getTableName,
    sizeBytes = t.getTableSizeBytes,
    itemCount = t.getItemCount,
    status = aws.model.TableStatus.fromValue(t.getTableStatus),
    attributes = Option(t.getAttributeDefinitions)
      .map(_.asScala.map(a => AttributeDefinition(a)).toSeq)
      .getOrElse(Nil),
    keySchema = Option(t.getKeySchema)
      .map(_.asScala.map(s => KeySchema(s)).toSeq)
      .getOrElse(Nil),
    globalSecondaryIndexes = Option(t.getGlobalSecondaryIndexes).map(_.asScala.toSeq).getOrElse(Seq.empty),
    localSecondaryIndexes = Option(t.getLocalSecondaryIndexes).map { indexes =>
      indexes.asScala.map(i => LocalSecondaryIndexMeta(i))
    }.getOrElse(Nil).toSeq,
    provisionedThroughput = ProvisionedThroughputMeta(t.getProvisionedThroughput),
    createdAt = new DateTime(t.getCreationDateTime),
    billingModeSummary = Option(t.getBillingModeSummary).map(BillingModeSummary.apply))
}

case class TableMeta(
  name: String,
  sizeBytes: Long,
  itemCount: Long,
  status: TableStatus,
  attributes: Seq[AttributeDefinition],
  keySchema: Seq[KeySchema],
  globalSecondaryIndexes: Seq[aws.model.GlobalSecondaryIndexDescription],
  localSecondaryIndexes: Seq[LocalSecondaryIndexMeta],
  provisionedThroughput: ProvisionedThroughputMeta,
  billingModeSummary: Option[BillingModeSummary],
  createdAt: DateTime) extends aws.model.TableDescription {

  def table: Table = Table(
    name = name,
    hashPK = keySchema.find(_.keyType == aws.model.KeyType.HASH).get.attributeName,
    rangePK = keySchema.find(_.keyType == aws.model.KeyType.RANGE).map(_.attributeName),
    attributes = attributes,
    globalSecondaryIndexes = globalSecondaryIndexes.map(GlobalSecondaryIndex.apply),
    localSecondaryIndexes = localSecondaryIndexes.map(e => LocalSecondaryIndex(e)),
    provisionedThroughput = Some(ProvisionedThroughput(provisionedThroughput)),
    billingMode = billingModeSummary.map(_.billingMode).map(aws.model.BillingMode.fromValue))

  setAttributeDefinitions(attributes.map(_.asInstanceOf[aws.model.AttributeDefinition]).asJava)
  setCreationDateTime(createdAt.toDate)
  setItemCount(itemCount)
  setKeySchema(keySchema.map(_.asInstanceOf[aws.model.KeySchemaElement]).asJava)
  setGlobalSecondaryIndexes(globalSecondaryIndexes.asJava)
  setLocalSecondaryIndexes(localSecondaryIndexes.map(_.asInstanceOf[aws.model.LocalSecondaryIndexDescription]).asJava)
  setProvisionedThroughput(provisionedThroughput)
  setTableName(name)
  setTableSizeBytes(sizeBytes)
  setTableStatus(status)
  billingModeSummary.foreach(setBillingModeSummary)
}

object LocalSecondaryIndexMeta {
  def apply(i: aws.model.LocalSecondaryIndexDescription): LocalSecondaryIndexMeta = new LocalSecondaryIndexMeta(
    name = i.getIndexName,
    sizeBytes = i.getIndexSizeBytes,
    itemCount = i.getItemCount,
    keySchema = i.getKeySchema.asScala.toSeq.map(k => KeySchema(k)),
    projection = Projection(i.getProjection))
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
    lastIncreasedAt = new DateTime(p.getLastIncreaseDateTime))
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

object BillingModeSummary {
  def apply(p: aws.model.BillingModeSummary): BillingModeSummary = new BillingModeSummary(
    billingMode = p.getBillingMode,
    lastUpdateToPayPerRequestDateTime = new DateTime(p.getLastUpdateToPayPerRequestDateTime))
}
case class BillingModeSummary(
  billingMode: String,
  lastUpdateToPayPerRequestDateTime: DateTime) extends aws.model.BillingModeSummary {

  setBillingMode(billingMode)
  setLastUpdateToPayPerRequestDateTime(lastUpdateToPayPerRequestDateTime.toDate)
}
