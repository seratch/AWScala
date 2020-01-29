package awscala.s3

import scala.jdk.CollectionConverters._
import com.amazonaws.services.{ s3 => aws }

object AccessControlList {
  def apply(acl: aws.model.AccessControlList): AccessControlList = new AccessControlList(acl)
}

class AccessControlList(acl: aws.model.AccessControlList) extends aws.model.AccessControlList {

  def grants: Set[Grant] = acl.getGrants.asScala.map(g => Grant(g)).toSet
  def owner: Owner = Owner(acl.getOwner)
  def grantAll(grants: Grant*): Unit = acl.grantAllPermissions(grants: _*)

  def grant(grantee: Grantee, permission: Permission) = acl.grantPermission(grantee, permission)
  def revokeAll(grantee: Grantee) = acl.revokeAllPermissions(grantee)
  def owner(newOwner: Owner) = acl.setOwner(newOwner)
}
