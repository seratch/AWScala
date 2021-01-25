package awscala.iam

import com.amazonaws.services.{ identitymanagement => aws }
import awscala.Policy

object GroupPolicy {
  def apply(group: Group, r: aws.model.GetGroupPolicyResult): GroupPolicy = GroupPolicy(
    group = group,
    name = r.getPolicyName,
    document = r.getPolicyDocument)
}

case class GroupPolicy(group: Group, name: String, document: String) {

  def this(group: Group, name: String, document: Policy) = {
    this(group, name, document.asJSON)
  }

  def destroy()(implicit iam: IAM): Unit = iam.delete(this)
}

