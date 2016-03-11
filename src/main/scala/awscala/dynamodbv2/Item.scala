package awscala.dynamodbv2

import com.amazonaws.services.{ dynamodbv2 ⇒ aws }

import scala.collection.JavaConversions._
import scala.collection.immutable

object Item {
  def apply(attributes: java.util.Map[String, aws.model.AttributeValue]): Item = new Item(
    attributes = attributes.toMap.mapValues(AttributeValue(_))
  )
}
case class Item(attributes: immutable.Map[String, AttributeValue])

