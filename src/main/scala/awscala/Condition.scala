package awscala

import scala.collection.JavaConverters._
import com.amazonaws.auth.{ policy => aws }

class Condition(val key: String, val typeName: String, values: Seq[String]) extends aws.Condition {
  setConditionKey(key)
  setType(typeName)
  setValues(values.asJava)

  def specifiedValues: Seq[String] = getValues.asScala
}
