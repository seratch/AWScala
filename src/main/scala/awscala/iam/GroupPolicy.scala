package awscala.iam

import com.amazonaws.services.{ identitymanagement => aws }

object GroupPolicy {
  def apply(group: Group, r: aws.model.GetGroupPolicyResult): GroupPolicy = GroupPolicy(
    group = group,
    name = r.getPolicyName,
    document = r.getPolicyDocument
  )
}

case class GroupPolicy(group: Group, name: String, document: String) {

  def this(group: Group, name: String, document: awscala.auth.policy.Policy) {
    this(group, name, document.asJSON)
  }

  def destroy()(implicit iam: IAM) = iam.delete(this)
}

