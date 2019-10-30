package awscala.dynamodbv2

import scala.jdk.CollectionConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }

object LocalSecondaryIndex {

  def apply(v: aws.model.LocalSecondaryIndexDescription): LocalSecondaryIndex = new LocalSecondaryIndex(
    name = v.getIndexName,
    keySchema = v.getKeySchema.asScala.map(k => KeySchema(k)).toSeq,
    projection = Projection(v.getProjection))
}

case class LocalSecondaryIndex(
  name: String,
  keySchema: Seq[KeySchema],
  projection: Projection) extends aws.model.LocalSecondaryIndex with SecondaryIndex {

  setIndexName(name)
  setKeySchema(keySchema.map(_.asInstanceOf[aws.model.KeySchemaElement]).asJava)
  setProjection(projection)
}
