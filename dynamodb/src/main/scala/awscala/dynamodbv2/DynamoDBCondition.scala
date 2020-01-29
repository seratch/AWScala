package awscala.dynamodbv2

import com.amazonaws.services.dynamodbv2.model.{ Condition, ExpectedAttributeValue }

import scala.jdk.CollectionConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }

trait DynamoCompares[A] {

  def value: A

  def withComparisonOperator(o: aws.model.ComparisonOperator): DynamoCompares[A]
  def withAttributeValueList(vs: java.util.Collection[aws.model.AttributeValue]): DynamoCompares[A]
}

class EACompares(ea: aws.model.ExpectedAttributeValue) extends DynamoCompares[aws.model.ExpectedAttributeValue] {
  val value: ExpectedAttributeValue = ea
  def withComparisonOperator(o: aws.model.ComparisonOperator) =
    new EACompares(ea.withComparisonOperator(o))
  def withAttributeValueList(vs: java.util.Collection[aws.model.AttributeValue]) =
    new EACompares(ea.withAttributeValueList(vs))
}

class CondCompares(c: aws.model.Condition) extends DynamoCompares[aws.model.Condition] {
  val value: Condition = c
  def withComparisonOperator(o: aws.model.ComparisonOperator) =
    new CondCompares(c.withComparisonOperator(o))
  def withAttributeValueList(vs: java.util.Collection[aws.model.AttributeValue]) =
    new CondCompares(c.withAttributeValueList(vs))
}

trait DynamoConditions[A] {
  def cond: DynamoCompares[A]

  def eq(values: Any*): A = cond
    .withComparisonOperator(aws.model.ComparisonOperator.EQ)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def ne(values: Any*): A = cond
    .withComparisonOperator(aws.model.ComparisonOperator.NE)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def gt(values: Any*): A = cond
    .withComparisonOperator(aws.model.ComparisonOperator.GT)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def ge(values: Any*): A = cond
    .withComparisonOperator(aws.model.ComparisonOperator.GE)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def lt(values: Any*): A = cond
    .withComparisonOperator(aws.model.ComparisonOperator.LT)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def le(values: Any*): A = cond
    .withComparisonOperator(aws.model.ComparisonOperator.LE)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def in(values: Any*): A = cond
    .withComparisonOperator(aws.model.ComparisonOperator.IN)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def between(values: Any*): A = cond
    .withComparisonOperator(aws.model.ComparisonOperator.BETWEEN)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def isNotNull: A = cond
    .withComparisonOperator(aws.model.ComparisonOperator.NOT_NULL)
    .value

  def isNull: A = cond
    .withComparisonOperator(aws.model.ComparisonOperator.NULL)
    .value

  def contains(values: Any*): A = cond
    .withComparisonOperator(aws.model.ComparisonOperator.CONTAINS)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def notContains(values: Any*): A = cond
    .withComparisonOperator(aws.model.ComparisonOperator.NOT_CONTAINS)
    .withAttributeValueList(values.map(v => AttributeValue.toJavaValue(v)).asJava)
    .value

  def beginsWith(values: Any*): A = cond
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
