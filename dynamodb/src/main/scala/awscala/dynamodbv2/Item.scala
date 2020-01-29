package awscala.dynamodbv2

import scala.jdk.CollectionConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }

object Item {
  def apply(table: Table, attributes: java.util.Map[String, aws.model.AttributeValue]): Item = new Item(
    table = table,
    attributes = attributes.asScala.toSeq.map { case (k, v) => Attribute(k, AttributeValue(v)) })
}

case class Item(table: Table, attributes: Seq[Attribute])

