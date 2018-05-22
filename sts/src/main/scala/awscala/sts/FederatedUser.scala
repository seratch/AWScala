package awscala.sts

import com.amazonaws.services.{ securitytoken => aws }

object FederatedUser {
  def apply(u: aws.model.FederatedUser): FederatedUser = new FederatedUser(
    arn = u.getArn,
    userId = u.getFederatedUserId)
}
case class FederatedUser(arn: String, userId: String) extends aws.model.FederatedUser {
  setArn(arn)
  setFederatedUserId(userId)
}

