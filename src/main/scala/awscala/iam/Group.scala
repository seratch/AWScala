package awscala.iam

import awscala._
import com.amazonaws.services.{ identitymanagement => aws }

object Group {

  def apply(g: aws.model.Group): Group = new Group(
    id = g.getGroupId,
    name = g.getGroupName,
    arn = g.getArn,
    path = g.getPath,
    createdAt = new DateTime(g.getCreateDate)
  )
}

case class Group(id: String, name: String, arn: String, path: String, createdAt: DateTime)
    extends aws.model.Group(path, name, id, arn, createdAt.toDate) {

  def updateName(newName: String)(implicit iam: IAM) = iam.updateGroupName(this, newName)
  def updatePath(newPath: String)(implicit iam: IAM) = iam.updateGroupPath(this, newPath)

  // users
  def add(user: User)(implicit iam: IAM) = addUser(user)
  def addUser(user: User)(implicit iam: IAM) = iam.addUserToGroup(this, user)
  def remove(user: User)(implicit iam: IAM) = removeUser(user)
  def removeUser(user: User)(implicit iam: IAM) = iam.removeUserFromGroup(this, user)

  // policies
  def policyNames()(implicit iam: IAM) = iam.groupPolicyNames(this)
  def policy(name: String)(implicit iam: IAM) = iam.groupPolicy(this, name)
  def putPolicy(name: String, document: String)(implicit iam: IAM) = iam.putGroupPolicy(this, name, document)
  def remove(policy: GroupPolicy)(implicit iam: IAM) = removePolicy(policy)
  def removePolicy(policy: GroupPolicy)(implicit iam: IAM) = iam.deleteGroupPolicy(policy)

  def destroy()(implicit iam: IAM) = iam.delete(this)
}

