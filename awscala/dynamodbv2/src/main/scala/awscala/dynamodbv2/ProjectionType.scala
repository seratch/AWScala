package awscala.dynamodbv2

import com.amazonaws.services.{ dynamodbv2 => aws }

object ProjectionType {

  val All = aws.model.ProjectionType.ALL
  val Include = aws.model.ProjectionType.INCLUDE
  val KeysOnly = aws.model.ProjectionType.KEYS_ONLY

}

