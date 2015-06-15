package awscala.dynamodbv2

import scala.collection.JavaConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }

object DynamoDBCondition {

  def eq(values: Any*) = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.EQ)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)

  def ne(values: Any*) = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.NE)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)

  def gt(values: Any*) = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.GT)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)

  def ge(values: Any*) = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.GE)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)

  def lt(values: Any*) = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.LT)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)

  def le(values: Any*) = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.LE)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)

  def in(values: Any*) = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.IN)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)

  def between(values: Any*) = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.BETWEEN)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)

  def isNotNull = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.NOT_NULL)

  def isNull = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.NULL)

  def contains(values: Any*) = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.CONTAINS)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)

  def notContains(values: Any*) = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.NOT_CONTAINS)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)

  def beginsWith(values: Any*) = new aws.model.Condition()
    .withComparisonOperator(aws.model.ComparisonOperator.BEGINS_WITH)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)

}

