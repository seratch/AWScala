package awscala.iam

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ identitymanagement => aws }

object InstanceProfile {

  def apply(g: aws.model.InstanceProfile): InstanceProfile = new InstanceProfile(
    id = g.getInstanceProfileId,
    name = g.getInstanceProfileName,
    arn = g.getArn,
    path = g.getPath,
    roles = g.getRoles.asScala.map(r => Role(r)),
    createdAt = new DateTime(g.getCreateDate)
  )
}

case class InstanceProfile(id: String, name: String, arn: String, path: String, roles: Seq[Role], createdAt: DateTime)
    extends aws.model.InstanceProfile {

  setArn(arn)
  setCreateDate(createdAt.toDate)
  setInstanceProfileId(id)
  setInstanceProfileName(name)
  setPath(path)
  setRoles(roles.map(_.asInstanceOf[aws.model.Role]).asJava)

  def add(role: Role)(implicit iam: IAM) = addRole(role)
  def addRole(role: Role)(implicit iam: IAM) = iam.addRoleToInstanceProfile(this, role)

  def remove(role: Role)(implicit iam: IAM) = removeRole(role)
  def removeRole(role: Role)(implicit iam: IAM) = iam.removeRoleFromInstanceProfile(this, role)

  def destroy()(implicit iam: IAM) = iam.delete(this)
}

