package awscala

import com.amazonaws.services.{ dynamodbv2 => aws }

package object dynamodbv2 extends DynamoDBImplicits {

  type TableStatus = aws.model.TableStatus
  type KeyType = aws.model.KeyType
  type AttributeAction = aws.model.AttributeAction
  type ProjectionType = aws.model.ProjectionType
  type ReturnConsumedCapacity = aws.model.ReturnConsumedCapacity

  type ComparisonOperator = aws.model.ComparisonOperator
  type Select = aws.model.Select

  val cond: DynamoDBCondition.type = awscala.dynamodbv2.DynamoDBCondition

}

