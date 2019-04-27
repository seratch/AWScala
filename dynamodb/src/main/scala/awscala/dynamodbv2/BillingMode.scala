package awscala.dynamodbv2

import com.amazonaws.services.{ dynamodbv2 => aws }

object BillingMode {

  val Provisioned = aws.model.BillingMode.PROVISIONED
  val PayPerRequest = aws.model.BillingMode.PAY_PER_REQUEST

}

