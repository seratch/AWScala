package awscala.iam

import com.amazonaws.services.{ identitymanagement => aws }

object UserPolicy {
  def apply(user: User, r: aws.model.GetUserPolicyResult): UserPolicy = UserPolicy(
    user = user,
    name = r.getPolicyName,
    document = r.getPolicyDocument
  )
}

case class UserPolicy(user: User, name: String, document: String) {

  def destroy()(implicit iam: IAM) = iam.delete(this)
}

