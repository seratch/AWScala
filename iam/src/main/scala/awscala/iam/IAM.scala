package awscala.iam

import awscala._
import scala.jdk.CollectionConverters._
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.{ identitymanagement => aws }

object IAM {
  def apply(credentials: Credentials): IAM = new IAMClient(BasicCredentialsProvider(credentials.getAWSAccessKeyId, credentials.getAWSSecretKey))
  def apply(credentialsProvider: AWSCredentialsProvider = CredentialsLoader.load()): IAM = new IAMClient(credentialsProvider)
  def apply(accessKeyId: String, secretAccessKey: String): IAM = {
    new IAMClient(BasicCredentialsProvider(accessKeyId, secretAccessKey))
  }
}

/**
 * Amazon Identity Management Java client wrapper
 * @see [[http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/]]
 */
trait IAM extends aws.AmazonIdentityManagement {

  // TODO AccountSummary
  // TODO AccountPasswordPolicy
  // TODO ServerCertificate
  // TODO SigningCertificates

  def changePassword(oldPassword: String, newPassword: String): Unit = {
    changePassword(new aws.model.ChangePasswordRequest().withOldPassword(oldPassword).withNewPassword(newPassword))
  }

  // ------------------------------------------
  // Account Aliases
  // ------------------------------------------

  def accountAliases: Seq[String] = listAccountAliases.getAccountAliases.asScala.toSeq
  def createAccountAlias(alias: String): Unit = {
    createAccountAlias(new aws.model.CreateAccountAliasRequest().withAccountAlias(alias))
  }
  def deleteAccountAlias(alias: String): Unit = {
    deleteAccountAlias(new aws.model.DeleteAccountAliasRequest().withAccountAlias(alias))
  }

  // ------------------------------------------
  // Groups
  // ------------------------------------------

  def groups: Seq[Group] = listGroups.getGroups.asScala.map(g => Group(g)).toSeq
  def groups(user: User): Seq[Group] = {
    listGroupsForUser(new aws.model.ListGroupsForUserRequest().withUserName(user.name))
      .getGroups.asScala.map(g => Group(g)).toSeq
  }
  def group(name: String): Option[Group] = try {
    Some(Group(getGroup(new aws.model.GetGroupRequest(name)).getGroup))
  } catch { case e: aws.model.NoSuchEntityException => None }

  def createGroup(name: String): Group = Group(createGroup(new aws.model.CreateGroupRequest(name)).getGroup)

  def updateGroupPath(group: Group, newPath: String): Unit = {
    updateGroup(new aws.model.UpdateGroupRequest().withGroupName(group.name).withNewPath(newPath))
  }
  def updateGroupName(group: Group, newName: String): Unit = {
    updateGroup(new aws.model.UpdateGroupRequest().withGroupName(group.name).withNewGroupName(newName))
  }

  def addUserToGroup(group: Group, user: User): Unit = {
    addUserToGroup(new aws.model.AddUserToGroupRequest(group.name, user.name))
  }
  def removeUserFromGroup(group: Group, user: User): Unit = {
    removeUserFromGroup(new aws.model.RemoveUserFromGroupRequest()
      .withGroupName(group.name).withUserName(user.name))
  }

  def delete(group: Group): Unit = deleteGroup(group)
  def deleteGroup(group: Group): Unit = deleteGroup(new aws.model.DeleteGroupRequest(group.name))

  // ------------------------------------------
  // Group Policies
  // ------------------------------------------

  def policyNames(group: Group) = groupPolicyNames(group)
  def groupPolicyNames(group: Group): Seq[String] = {
    listGroupPolicies(new aws.model.ListGroupPoliciesRequest().withGroupName(group.name)).getPolicyNames.asScala.toSeq
  }

  def groupPolicy(group: Group, policyName: String): GroupPolicy = GroupPolicy(
    group, getGroupPolicy(new aws.model.GetGroupPolicyRequest().withGroupName(group.name).withPolicyName(policyName)))

  def put(policy: GroupPolicy): Unit = putGroupPolicy(policy)
  def putGroupPolicy(policy: GroupPolicy): Unit = putGroupPolicy(policy.group, policy.name, policy.document)
  def putGroupPolicy(group: Group, policyName: String, policy: Policy): Unit = {
    putGroupPolicy(group, policyName, policy.toJSON)
  }
  def putGroupPolicy(group: Group, policyName: String, policyDocument: String): Unit = {
    putGroupPolicy(new aws.model.PutGroupPolicyRequest()
      .withGroupName(group.name)
      .withPolicyName(policyName).withPolicyDocument(policyDocument))
  }

  def delete(policy: GroupPolicy): Unit = deleteGroupPolicy(policy)
  def deleteGroupPolicy(policy: GroupPolicy): Unit = {
    deleteGroupPolicy(
      new aws.model.DeleteGroupPolicyRequest().withGroupName(policy.group.name).withPolicyName(policy.name))
  }

  // ------------------------------------------
  // Users
  // ------------------------------------------

  def users: Seq[User] = listUsers.getUsers.asScala.map(u => User(u)).toSeq
  def user(name: String): Option[User] = try {
    Option(User(getUser(new aws.model.GetUserRequest().withUserName(name)).getUser))
  } catch { case e: aws.model.NoSuchEntityException => None }

  def createUser(name: String): User = User(createUser(new aws.model.CreateUserRequest(name)).getUser)

  def updateUserName(user: User, newName: String): Unit = {
    updateUser(new aws.model.UpdateUserRequest().withUserName(user.name).withNewUserName(newName))
  }
  def updateUserPath(user: User, newPath: String): Unit = {
    updateUser(new aws.model.UpdateUserRequest().withUserName(user.name).withNewPath(newPath))
  }

  def delete(user: User): Unit = deleteUser(user)
  def deleteUser(user: User): Unit = deleteUser(new aws.model.DeleteUserRequest(user.name))

  // ------------------------------------------
  // User Policies
  // ------------------------------------------

  def policyNames(user: User) = userPolicyNames(user)
  def userPolicyNames(user: User): Seq[String] = {
    listUserPolicies(new aws.model.ListUserPoliciesRequest().withUserName(user.name)).getPolicyNames.asScala.toSeq
  }

  def put(policy: UserPolicy): Unit = putUserPolicy(policy)
  def putUserPolicy(policy: UserPolicy): Unit = putUserPolicy(policy.user, policy.name, policy.document)
  def putUserPolicy(user: User, policyName: String, policyDocument: String): Unit = {
    putUserPolicy(new aws.model.PutUserPolicyRequest()
      .withUserName(user.name)
      .withPolicyName(policyName).withPolicyDocument(policyDocument))
  }

  def userPolicy(user: User, policyName: String): Option[UserPolicy] = try {
    Option(UserPolicy(
      user, getUserPolicy(new aws.model.GetUserPolicyRequest().withUserName(user.name).withPolicyName(policyName))))
  } catch { case e: aws.model.NoSuchEntityException => None }

  def delete(policy: UserPolicy): Unit = deleteUserPolicy(policy)
  def deleteUserPolicy(policy: UserPolicy): Unit = {
    deleteUserPolicy(
      new aws.model.DeleteUserPolicyRequest().withUserName(policy.user.name).withPolicyName(policy.name))
  }

  // ------------------------------------------
  // Access Keys
  // ------------------------------------------

  def accessKeys: Seq[AccessKey] = listAccessKeys.getAccessKeyMetadata.asScala.map(meta => AccessKey(meta)).toSeq
  def accessKeys(user: User): Seq[AccessKey] = {
    listAccessKeys(new aws.model.ListAccessKeysRequest().withUserName(user.name)).getAccessKeyMetadata
      .asScala.map(meta => AccessKey(meta)).toSeq
  }

  def createAccessKey(user: User): AccessKey = {
    AccessKey(createAccessKey(new aws.model.CreateAccessKeyRequest().withUserName(user.name)).getAccessKey)
  }

  def activateAccessKey(accessKey: AccessKey): Unit = {
    updateAccessKey(new aws.model.UpdateAccessKeyRequest()
      .withAccessKeyId(accessKey.accessKeyId).withStatus(aws.model.StatusType.Active))
  }
  def inactivateAccessKey(accessKey: AccessKey): Unit = {
    updateAccessKey(new aws.model.UpdateAccessKeyRequest()
      .withAccessKeyId(accessKey.accessKeyId).withStatus(aws.model.StatusType.Inactive))
  }

  def delete(accessKey: AccessKey) = deleteAccessKey(accessKey)
  def deleteAccessKey(accessKey: AccessKey): Unit = {
    deleteAccessKey(new aws.model.DeleteAccessKeyRequest(accessKey.userName, accessKey.accessKeyId))
  }

  // ------------------------------------------
  // Roles
  // ------------------------------------------

  def roles: Seq[Role] = listRoles.getRoles.asScala.map(r => Role(r)).toSeq

  def createRole(name: String, path: String, assumeRolePolicy: Policy): Role = {
    createRole(name, path, assumeRolePolicy.toJSON)
  }
  def createRole(name: String, path: String, assumeRolePolicyDocument: String): Role = {
    Role(createRole(new aws.model.CreateRoleRequest()
      .withRoleName(name)
      .withPath(path)
      .withAssumeRolePolicyDocument(assumeRolePolicyDocument)).getRole)
  }

  def delete(role: Role): Unit = deleteRole(role)
  def deleteRole(role: Role): Unit = {
    deleteRole(new aws.model.DeleteRoleRequest().withRoleName(role.name))
  }

  // ------------------------------------------
  // Role Policies
  // ------------------------------------------

  def policyNames(role: Role) = rolePolicyNames(role)
  def rolePolicyNames(role: Role): Seq[String] = {
    listRolePolicies(new aws.model.ListRolePoliciesRequest().withRoleName(role.name)).getPolicyNames.asScala.toSeq
  }

  def put(policy: RolePolicy): Unit = putRolePolicy(policy)
  def putRolePolicy(policy: RolePolicy): Unit = putRolePolicy(policy.role, policy.name, policy.document)
  def putRolePolicy(role: Role, policyName: String, policy: Policy): Unit = {
    putRolePolicy(role, policyName, policy.toJSON)
  }
  def putRolePolicy(role: Role, policyName: String, policyDocument: String): Unit = {
    putRolePolicy(new aws.model.PutRolePolicyRequest()
      .withRoleName(role.name)
      .withPolicyName(policyName).withPolicyDocument(policyDocument))
  }

  def rolePolicy(role: Role, policyName: String): RolePolicy = RolePolicy(
    role, getRolePolicy(new aws.model.GetRolePolicyRequest().withRoleName(role.name).withPolicyName(policyName)))

  def delete(policy: RolePolicy): Unit = deleteRolePolicy(policy)
  def deleteRolePolicy(policy: RolePolicy): Unit = {
    deleteRolePolicy(
      new aws.model.DeleteRolePolicyRequest().withRoleName(policy.role.name).withPolicyName(policy.name))
  }

  // ------------------------------------------
  // Instance Profiles
  // ------------------------------------------

  def instanceProfiles: Seq[InstanceProfile] = {
    listInstanceProfiles.getInstanceProfiles.asScala.map(p => InstanceProfile(p)).toSeq
  }
  def instanceProfiles(role: Role): Seq[InstanceProfile] = {
    listInstanceProfilesForRole(new aws.model.ListInstanceProfilesForRoleRequest().withRoleName(role.name))
      .getInstanceProfiles.asScala.map(p => InstanceProfile(p)).toSeq
  }

  def createInstanceProfile(name: String, path: String): InstanceProfile = {
    InstanceProfile(createInstanceProfile(
      new aws.model.CreateInstanceProfileRequest().withInstanceProfileName(name).withPath(path)).getInstanceProfile)
  }

  def addRoleToInstanceProfile(profile: InstanceProfile, role: Role): Unit = {
    addRoleToInstanceProfile(new aws.model.AddRoleToInstanceProfileRequest()
      .withInstanceProfileName(profile.name).withRoleName(role.name))
  }
  def removeRoleFromInstanceProfile(profile: InstanceProfile, role: Role): Unit = {
    removeRoleFromInstanceProfile(new aws.model.RemoveRoleFromInstanceProfileRequest()
      .withInstanceProfileName(profile.name).withRoleName(role.name))
  }
  def delete(profile: InstanceProfile): Unit = deleteInstanceProfile(profile)
  def deleteInstanceProfile(profile: InstanceProfile): Unit = {
    deleteInstanceProfile(
      new aws.model.DeleteInstanceProfileRequest().withInstanceProfileName(profile.name))
  }

  // ------------------------------------------
  // Login Profiles
  // ------------------------------------------

  def createLoginProfile(user: User, password: String): LoginProfile = {
    LoginProfile(
      user,
      createLoginProfile(new aws.model.CreateLoginProfileRequest().withUserName(user.name).withPassword(password)).getLoginProfile)
  }

  def loginProfile(user: User): Option[LoginProfile] = try {
    Option(LoginProfile(user, getLoginProfile(new aws.model.GetLoginProfileRequest().withUserName(user.name)).getLoginProfile))
  } catch { case e: aws.model.NoSuchEntityException => None }

  def changeUserPassword(profile: LoginProfile, newPassword: String): Unit = {
    updateLoginProfile(new aws.model.UpdateLoginProfileRequest()
      .withUserName(profile.user.name).withPassword(newPassword))
  }

  def delete(profile: LoginProfile): Unit = deleteLoginProfile(profile)
  def deleteLoginProfile(profile: LoginProfile): Unit = {
    deleteLoginProfile(
      new aws.model.DeleteLoginProfileRequest().withUserName(profile.user.name))
  }

  // ------------------------------------------
  // Virtual MFA Devices
  // ------------------------------------------

  def virtualMFADevices: Seq[VirtualMFADevice] = {
    listVirtualMFADevices.getVirtualMFADevices.asScala.map(d => VirtualMFADevice(d)).toSeq
  }
  def virtualMFADevices(user: User): Seq[VirtualMFADevice] = {
    listMFADevices(new aws.model.ListMFADevicesRequest().withUserName(user.name)).getMFADevices.asScala
      .map(d => VirtualMFADevice(user, d)).toSeq
  }

  def createVirtualMFADevice(name: String, path: String): VirtualMFADevice = {
    VirtualMFADevice(createVirtualMFADevice(
      new aws.model.CreateVirtualMFADeviceRequest().withVirtualMFADeviceName(name).withPath(path)).getVirtualMFADevice)
  }

  def enableVirtualMFADevice(device: VirtualMFADevice, user: User, authCode1: String, authCode2: String) = {
    enableMFADevice(
      new aws.model.EnableMFADeviceRequest().withUserName(user.name).withSerialNumber(device.serialNumber)
        .withAuthenticationCode1(authCode1).withAuthenticationCode2(authCode2))
  }

  def disableVirtualMFADevice(device: VirtualMFADevice, user: User): Unit = {
    deactivateMFADevice(
      new aws.model.DeactivateMFADeviceRequest().withSerialNumber(device.serialNumber).withUserName(user.name))
  }

  def delete(device: VirtualMFADevice): Unit = deleteVirtualMFADevice(device)
  def deleteVirtualMFADevice(device: VirtualMFADevice): Unit = {
    deleteVirtualMFADevice(new aws.model.DeleteVirtualMFADeviceRequest().withSerialNumber(device.serialNumber))
  }

}

/**
 * Default Implementation
 *
 * @param credentialsProvider credentialsProvider
 */
class IAMClient(credentialsProvider: AWSCredentialsProvider = CredentialsLoader.load())
  extends aws.AmazonIdentityManagementClient(credentialsProvider)
  with IAM
