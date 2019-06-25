package awscala.dynamodbv2

import com.amazonaws.services.{ dynamodbv2 => aws }

trait DynamoDBImplicits {
  implicit class RichScalaAttributeValue(v: AttributeValue) {
    def asJava: aws.model.AttributeValue = AttributeValue.toJavaValue(v)
  }

  implicit class RichJavaAttributeValue(v: aws.model.AttributeValue) {
    def asScala: AttributeValue = AttributeValue(v)
  }
}

object DynamoDBImplicits extends DynamoDBImplicits