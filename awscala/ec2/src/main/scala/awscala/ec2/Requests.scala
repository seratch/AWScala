package awscala.ec2

import scala.collection.JavaConverters._
import com.amazonaws.services.{ ec2 => aws }

case class RunInstancesRequest(imageId: String, min: Int = 1, max: Int = 1)
    extends aws.model.RunInstancesRequest(imageId, min, max) {

  // TODO set...
}

object KeyPair {
  def apply(k: aws.model.KeyPair): KeyPair = KeyPair(k.getKeyName, k.getKeyFingerprint, Option(k.getKeyMaterial))
  def apply(k: aws.model.KeyPairInfo): KeyPair = KeyPair(k.getKeyName, k.getKeyFingerprint, None)
}
case class KeyPair(name: String, fingerprint: String, material: Option[String]) extends aws.model.KeyPair {
  override def getKeyName = name
  override def getKeyFingerprint = fingerprint
  override def getKeyMaterial = material.orNull
}

case class SecurityGroup(
  groupId: String,
  groupName: String,
  description: String,
  ipPermissions: Seq[IpPermission],
  ipPermissionsEgress: Seq[IpPermission],
  ownerId: String, tags: Map[String, String],
  vpcId: String
) extends aws.model.SecurityGroup

object SecurityGroup {
  def apply(k: aws.model.SecurityGroup): SecurityGroup =
    SecurityGroup(
      k.getGroupId,
      k.getGroupName,
      k.getDescription,
      k.getIpPermissions.asScala.map(IpPermission(_)),
      k.getIpPermissionsEgress.asScala.map(IpPermission(_)),
      k.getOwnerId,
      k.getTags.asScala.map(t => t.getKey -> t.getValue).toMap,
      k.getVpcId
    )
}

object IpPermission {
  def apply(i: aws.model.IpPermission): IpPermission = {
    IpPermission(
      fromPort = if (i.getFromPort == null) -1 else i.getFromPort,
      toPort = if (i.getToPort == null) -1 else i.getToPort,
      ipRanges = i.getIpRanges.asScala,
      ipProtocol = i.getIpProtocol,
      userIdGroupPairs = i.getUserIdGroupPairs.asScala.map(UserIdGroupPair(_))
    )

  }
}
case class IpPermission(
  fromPort: Int,
  toPort: Int,
  ipRanges: Seq[String],
  ipProtocol: String,
  userIdGroupPairs: Seq[UserIdGroupPair]
) extends aws.model.IpPermission

object UserIdGroupPair {
  def apply(u: aws.model.UserIdGroupPair): UserIdGroupPair = {
    UserIdGroupPair(
      groupId = u.getGroupId,
      groupName = u.getGroupName,
      userId = u.getUserId
    )
  }
}
case class UserIdGroupPair(groupId: String, groupName: String, userId: String)
  extends aws.model.UserIdGroupPair

