package awscala.dynamodbv2

import com.amazonaws.services.{ dynamodbv2 => aws }

object KeyType {

  val Hash = aws.model.KeyType.HASH
  val Range = aws.model.KeyType.RANGE

}

