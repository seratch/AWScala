package awscala.dynamodb

import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType

object AttributeType {

  val String = ScalarAttributeType.S
  val Number = ScalarAttributeType.N
  val Binary = ScalarAttributeType.B

}

