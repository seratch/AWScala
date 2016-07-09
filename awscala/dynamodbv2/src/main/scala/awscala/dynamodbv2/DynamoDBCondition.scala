package awscala.dynamodbv2

import scala.collection.JavaConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }

trait DynamoCompares[A] {

  def value: A

  def withComparisonOperator(o: aws.model.ComparisonOperator): DynamoCompares[A]
  def withAttributeValueList(vs: java.util.Collection[aws.model.AttributeValue]): DynamoCompares[A]
}

class EACompares(ea: aws.model.ExpectedAttributeValue) extends DynamoCompares[aws.model.ExpectedAttributeValue] {
  val value = ea
  def withComparisonOperator(o: aws.model.ComparisonOperator) =
    new EACompares(ea.withComparisonOperator(o))
  def withAttributeValueList(vs: java.util.Collection[aws.model.AttributeValue]) =
    new EACompares(ea.withAttributeValueList(vs))
}

class CondCompares(c: aws.model.Condition) extends DynamoCompares[aws.model.Condition] {
  val value = c
  def withComparisonOperator(o: aws.model.ComparisonOperator) =
    new CondCompares(c.withComparisonOperator(o))
  def withAttributeValueList(vs: java.util.Collection[aws.model.AttributeValue]) =
    new CondCompares(c.withAttributeValueList(vs))
}

trait DynamoConditions[A] {
  def cond: DynamoCompares[A]

  def eq(values: Any*) = cond
    .withComparisonOperator(aws.model.ComparisonOperator.EQ)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def ne(values: Any*) = cond
    .withComparisonOperator(aws.model.ComparisonOperator.NE)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def gt(values: Any*) = cond
    .withComparisonOperator(aws.model.ComparisonOperator.GT)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def ge(values: Any*) = cond
    .withComparisonOperator(aws.model.ComparisonOperator.GE)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def lt(values: Any*) = cond
    .withComparisonOperator(aws.model.ComparisonOperator.LT)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def le(values: Any*) = cond
    .withComparisonOperator(aws.model.ComparisonOperator.LE)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def in(values: Any*) = cond
    .withComparisonOperator(aws.model.ComparisonOperator.IN)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def between(values: Any*) = cond
    .withComparisonOperator(aws.model.ComparisonOperator.BETWEEN)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def isNotNull = cond
    .withComparisonOperator(aws.model.ComparisonOperator.NOT_NULL)
    .value

  def isNull = cond
    .withComparisonOperator(aws.model.ComparisonOperator.NULL)
    .value

  def contains(values: Any*) = cond
    .withComparisonOperator(aws.model.ComparisonOperator.CONTAINS)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def notContains(values: Any*) = cond
    .withComparisonOperator(aws.model.ComparisonOperator.NOT_CONTAINS)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def beginsWith(values: Any*) = cond
    .withComparisonOperator(aws.model.ComparisonOperator.BEGINS_WITH)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

}

object DynamoDBCondition extends DynamoConditions[aws.model.Condition] {
  def cond = new CondCompares(new aws.model.Condition)
}

object DynamoDBExpectedAttributeValue extends DynamoConditions[aws.model.ExpectedAttributeValue] {
  def cond = new EACompares(new aws.model.ExpectedAttributeValue)
}
