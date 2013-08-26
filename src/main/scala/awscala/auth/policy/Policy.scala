package awscala.auth.policy

import scala.collection.JavaConverters._
import com.amazonaws.auth.{ policy => aws }

case class Policy(statements: Seq[Statement], id: Option[String] = None) extends aws.Policy {
  id.foreach(i => setId(i))
  setStatements(statements.map(_.asInstanceOf[aws.Statement]).asJava)

  def version = getVersion

  def toJSON = toJson
  def asJson = toJson
  def asJSON = toJson
}

case class Statement(
    effect: aws.Statement.Effect,
    actions: Seq[Action],
    resources: Seq[Resource],
    id: Option[String] = None,
    conditions: Seq[Condition] = Nil,
    principals: Seq[aws.Principal] = Nil) extends aws.Statement(effect) {

  id.foreach(i => setId(i))
  setEffect(effect)
  setActions(actions.map(_.asInstanceOf[aws.Action]).asJava)
  setConditions(conditions.map(_.asInstanceOf[aws.Condition]).asJava)
  setPrincipals(principals.asJava)
  setResources(resources.map(_.asInstanceOf[aws.Resource]).asJava)
}

case class Action(name: String) extends aws.Action {
  override def getActionName = name
}

class Condition(val key: String, val typeName: String, values: Seq[String]) extends aws.Condition {
  setConditionKey(key)
  setType(typeName)
  setValues(values.asJava)

  def specifiedValues: Seq[String] = getValues.asScala
}

case class Resource(id: String) extends aws.Resource(id)

object Effect {
  val Allow = aws.Statement.Effect.Allow
  val Deny = aws.Statement.Effect.Deny
}
