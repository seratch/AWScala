package awscala.s3

import com.amazonaws.services.{ s3 => aws }

object Grant {
  def apply(g: aws.model.Grant): Grant = Grant(Grantee(g.getGrantee), g.getPermission)
}
case class Grant(grantee: Grantee, permission: Permission) extends aws.model.Grant(grantee, permission)

