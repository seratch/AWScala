package awscala.dynamodbv2

import scala.collection.JavaConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }

object GlobalSecondaryIndex {

  def apply(v: aws.model.GlobalSecondaryIndexDescription): GlobalSecondaryIndex = new GlobalSecondaryIndex(
    name = v.getIndexName,
    keySchema = v.getKeySchema.asScala.map(k => KeySchema(k)),
    projection = Projection(v.getProjection),
    provisionedThroughput = ProvisionedThroughput(v.getProvisionedThroughput.getReadCapacityUnits, v.getProvisionedThroughput.getWriteCapacityUnits))

}
case class GlobalSecondaryIndex(
  name: String,
  keySchema: Seq[KeySchema],
  projection: Projection,
  provisionedThroughput: ProvisionedThroughput) extends aws.model.GlobalSecondaryIndex with SecondaryIndex {

  setIndexName(name)
  setKeySchema(keySchema.map(_.asInstanceOf[aws.model.KeySchemaElement]).asJava)
  setProjection(projection)
  setProvisionedThroughput(provisionedThroughput)
}
