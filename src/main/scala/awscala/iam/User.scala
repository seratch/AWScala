package awscala.iam

import awscala._
import com.amazonaws.services.{ identitymanagement => aws }

object User {

  def apply(g: aws.model.User): User = new User(
    id = g.getUserId,
    name = g.getUserName,
    arn = g.getArn,
    path = g.getPath,
    createdAt = new DateTime(g.getCreateDate)
  )
}

case class User(id: String, name: String, arn: String, path: String, createdAt: DateTime)
    extends aws.model.User(path, name, id, arn, createdAt.toDate) {

  def updateName(name: String)(implicit iam: IAM) = iam.updateUserName(this, name)
  def updatePath(path: String)(implicit iam: IAM) = iam.updateUserPath(this, path)

  // login profile
  def setLoginPassword(password: String)(implicit iam: IAM) = iam.createLoginProfile(this, password)
  def loginProfile()(implicit iam: IAM): Option[LoginProfile] = iam.loginProfile(this)

  // groups
  def groups()(implicit iam: IAM): Seq[Group] = iam.groups(this)
  def join(group: Group)(implicit iam: IAM) = iam.addUserToGroup(group, this)
  def leave(group: Group)(implicit iam: IAM) = iam.removeUserFromGroup(group, this)

  // policies
  def policyNames()(implicit iam: IAM) = iam.policyNames(this)
  def policy(name: String)(implicit iam: IAM) = iam.userPolicy(this, name)
  def putPolicy(name: String, document: String)(implicit iam: IAM) = iam.putUserPolicy(this, name, document)
  def remove(policy: UserPolicy)(implicit iam: IAM) = removePolicy(policy)
  def removePolicy(policy: UserPolicy)(implicit iam: IAM) = iam.deleteUserPolicy(policy)

  // access keys
  def accessKeys()(implicit iam: IAM): Seq[AccessKey] = iam.accessKeys(this)
  def createAccessKey()(implicit iam: IAM): AccessKey = iam.createAccessKey(this)

  // MFA devices
  def virtualMFADevices()(implicit iam: IAM): Seq[VirtualMFADevice] = iam.virtualMFADevices(this)
  def add(device: VirtualMFADevice, code1: String, code2: String)(implicit iam: IAM) = {
    addVirtualMFADevice(device, code1, code2)
  }
  def addVirtualMFADevice(device: VirtualMFADevice, code1: String, code2: String)(implicit iam: IAM) = {
    iam.enableVirtualMFADevice(device, this, code1, code2)
  }
  def remove(device: VirtualMFADevice)(implicit iam: IAM) = removeVirtualMFADevice(device)
  def removeVirtualMFADevice(device: VirtualMFADevice)(implicit iam: IAM) = {
    iam.disableVirtualMFADevice(device, this)
  }

  def destroy()(implicit iam: IAM) = iam.delete(this)
}

