package awscala

import com.amazonaws.services.{ dynamodbv2 => aws }

package object dynamodb {

  type TableStatus = aws.model.TableStatus
  type KeyType = aws.model.KeyType
  type AttributeAction = aws.model.AttributeAction
  type ProjectionType = aws.model.ProjectionType
  type ReturnConsumedCapacity = aws.model.ReturnConsumedCapacity

  type Condition = aws.model.Condition
  type ComparisonOperator = aws.model.ComparisonOperator
  type Select = aws.model.Select

}

