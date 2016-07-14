package awscala.iam

import com.amazonaws.services.{ identitymanagement => aws }

object RolePolicy {
  def apply(role: Role, r: aws.model.GetRolePolicyResult): RolePolicy = RolePolicy(
    role = role,
    name = r.getPolicyName,
    document = r.getPolicyDocument
  )
}

case class RolePolicy(role: Role, name: String, document: String) {

  def destroy()(implicit iam: IAM) = iam.delete(this)
}

