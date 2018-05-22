package awscala

import com.amazonaws.auth.{ policy => aws }

case class Action(name: String) extends aws.Action {
  override def getActionName = name
}
