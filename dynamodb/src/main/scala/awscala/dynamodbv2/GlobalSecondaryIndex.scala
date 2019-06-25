package awscala.dynamodbv2

import scala.collection.JavaConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }

object GlobalSecondaryIndex {

  def apply(v: aws.model.GlobalSecondaryIndexDescription): GlobalSecondaryIndex = GlobalSecondaryIndex(
    name = v.getIndexName,
    keySchema = v.getKeySchema.asScala.map(k => KeySchema(k)).toSeq,
    projection = Projection(v.getProjection),
    provisionedThroughput =
      Option(v.getProvisionedThroughput)
        .map { pt => ProvisionedThroughput(pt.getReadCapacityUnits, pt.getWriteCapacityUnits) })

  def apply(
    name: String,
    keySchema: Seq[KeySchema],
    projection: Projection,
    provisionedThroughput: ProvisionedThroughput): GlobalSecondaryIndex =
    new GlobalSecondaryIndex(name, keySchema, projection, Option(provisionedThroughput))
}

case class GlobalSecondaryIndex(
  name: String,
  keySchema: Seq[KeySchema],
  projection: Projection,
  provisionedThroughput: Option[ProvisionedThroughput] = None) extends aws.model.GlobalSecondaryIndex with SecondaryIndex {

  setIndexName(name)
  setKeySchema(keySchema.map(_.asInstanceOf[aws.model.KeySchemaElement]).asJava)
  setProjection(projection)
  provisionedThroughput.foreach(setProvisionedThroughput)
}
