package awscala

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
