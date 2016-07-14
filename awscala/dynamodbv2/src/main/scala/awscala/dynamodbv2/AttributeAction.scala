package awscala.dynamodbv2

import com.amazonaws.services.{ dynamodbv2 => aws }

object AttributeAction {

  val Add = aws.model.AttributeAction.ADD
  val Put = aws.model.AttributeAction.PUT
  val Delete = aws.model.AttributeAction.DELETE

}

