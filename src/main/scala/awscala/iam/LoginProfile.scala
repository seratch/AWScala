package awscala.iam

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ identitymanagement => aws }

object LoginProfile {

  def apply(user: User, g: aws.model.LoginProfile): LoginProfile = new LoginProfile(
    user = user,
    createdAt = new DateTime(g.getCreateDate)
  )
}

case class LoginProfile(user: User, createdAt: DateTime)
    extends aws.model.LoginProfile {

  setUserName(user.name)
  setCreateDate(createdAt.toDate)

  def changePassword(newPassword: String)(implicit iam: IAM) = iam.changeUserPassword(this, newPassword)
  def destroy()(implicit iam: IAM) = iam.delete(this)
}

