package awscala.ec2

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ ec2 => aws }

case class RunInstancesRequest(imageId: String, min: Int = 1, max: Int = 1) extends aws.model.RunInstancesRequest(imageId, min, max)

case class KeyPair(name: String, fingerprint: String, material: Option[String]) extends aws.model.KeyPair
object KeyPair {
  def apply(k: aws.model.KeyPair): KeyPair = KeyPair(k.getKeyName, k.getKeyFingerprint, wrapOption(k.getKeyMaterial))
  def apply(k: aws.model.KeyPairInfo): KeyPair = KeyPair(k.getKeyName, k.getKeyFingerprint, None)
}
case class SecurityGroup(groupId: String,
  groupName: String,
  description: String,
  ipPermissions: Seq[IpPermission],
  ipPermissionsEgress: Seq[IpPermission],
  ownerId: String, tags: Map[String, String],
  vpcId: String) extends aws.model.SecurityGroup

object SecurityGroup {
  def apply(k: aws.model.SecurityGroup): SecurityGroup =
    SecurityGroup(k.getGroupId,
      k.getGroupName,
      k.getDescription,
      k.getIpPermissions.asScala.map(IpPermission(_)),
      k.getIpPermissionsEgress.asScala.map(IpPermission(_)),
      k.getOwnerId,
      k.getTags.asScala.map(t => t.getKey -> t.getValue).toMap,
      k.getVpcId)
}
case class IpPermission(fromPort: Int, toPort: Int, ipRanges: Seq[String], ipProtocol: String, userIdGroupPairs: Seq[UserIdGroupPair]) extends aws.model.IpPermission
object IpPermission {
  def apply(i: aws.model.IpPermission): IpPermission = IpPermission(i.getFromPort, i.getToPort, i.getIpRanges.asScala, i.getIpProtocol, i.getUserIdGroupPairs.asScala.map(UserIdGroupPair(_)))
}
case class UserIdGroupPair(groupId: String, groupName: String, userId: String) extends aws.model.UserIdGroupPair
object UserIdGroupPair {
  def apply(u: aws.model.UserIdGroupPair): UserIdGroupPair = UserIdGroupPair(u.getGroupId, u.getGroupName, u.getUserId)
}

