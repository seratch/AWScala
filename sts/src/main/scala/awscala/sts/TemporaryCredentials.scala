package awscala.sts

import awscala.DateTime.toDate
import awscala._
import com.amazonaws.services.{ securitytoken => aws }
import com.amazonaws.auth.{ AWSSessionCredentials, BasicSessionCredentials }

object TemporaryCredentials {
  def apply(c: aws.model.Credentials): TemporaryCredentials = new TemporaryCredentials(
    accessKeyId = c.getAccessKeyId,
    secretAccessKey = c.getSecretAccessKey,
    sessionToken = c.getSessionToken,
    expiration = DateTime(c.getExpiration))
}

case class TemporaryCredentials(
  accessKeyId: String,
  secretAccessKey: String,
  sessionToken: String,
  expiration: DateTime) extends aws.model.Credentials {

  setAccessKeyId(accessKeyId)
  setExpiration(toDate(expiration))
  setSecretAccessKey(secretAccessKey)
  setSessionToken(sessionToken)

  def toSessionCredentials: AWSSessionCredentials = {
    new BasicSessionCredentials(accessKeyId, secretAccessKey, sessionToken)
  }
}
