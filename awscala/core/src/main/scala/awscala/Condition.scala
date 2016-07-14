package awscala

import scala.collection.JavaConverters._
import com.amazonaws.auth.{ policy => aws }

case class Condition(key: String, typeName: String, conditionValues: Seq[String]) extends aws.Condition {
  setConditionKey(key)
  setType(typeName)
  setValues(conditionValues.asJava)

  def specifiedValues: Seq[String] = getValues.asScala
}
