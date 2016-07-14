package awscala.dynamodbv2

import com.amazonaws.services.{ dynamodbv2 => aws }

object AttributeDefinition {
  def apply(a: aws.model.AttributeDefinition): AttributeDefinition = new AttributeDefinition(
    name = a.getAttributeName,
    scalarType = aws.model.ScalarAttributeType.fromValue(a.getAttributeType)
  )
}
case class AttributeDefinition(name: String, scalarType: aws.model.ScalarAttributeType) extends aws.model.AttributeDefinition {
  setAttributeName(name)
  setAttributeType(scalarType)

}

