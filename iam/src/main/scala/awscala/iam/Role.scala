package awscala.iam

import awscala.DateTime.toDate
import awscala._
import com.amazonaws.services.{ identitymanagement => aws }

object Role {

  def apply(g: aws.model.Role): Role = new Role(
    id = g.getRoleId,
    name = g.getRoleName,
    arn = g.getArn,
    path = g.getPath,
    assumeRolePolicyDocument = g.getAssumeRolePolicyDocument,
    createdAt = DateTime(g.getCreateDate))
}

case class Role(id: String, name: String, arn: String, path: String, assumeRolePolicyDocument: String, createdAt: DateTime)
  extends aws.model.Role {

  setArn(arn)
  setAssumeRolePolicyDocument(assumeRolePolicyDocument)
  setCreateDate(toDate(createdAt))
  setPath(path)
  setRoleId(id)
  setRoleName(name)

  // instance profiles
  def instanceProfiles()(implicit iam: IAM): Seq[InstanceProfile] = iam.instanceProfiles(this)

  // policies
  def policyNames()(implicit iam: IAM) = iam.rolePolicyNames(this)
  def policy(name: String)(implicit iam: IAM) = iam.rolePolicy(this, name)
  def putPolicy(name: String, policy: Policy)(implicit iam: IAM) = iam.putRolePolicy(this, name, policy.toJSON)
  def putPolicy(name: String, document: String)(implicit iam: IAM) = iam.putRolePolicy(this, name, document)
  def remove(policy: RolePolicy)(implicit iam: IAM) = removePolicy(policy)
  def removePolicy(policy: RolePolicy)(implicit iam: IAM) = iam.deleteRolePolicy(policy)

  def destroy()(implicit iam: IAM) = iam.delete(this)
}

