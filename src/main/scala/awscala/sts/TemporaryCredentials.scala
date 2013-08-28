package awscala.sts

import awscala._
import com.amazonaws.services.{ securitytoken => aws }

object TemporaryCredentials {
  def apply(c: aws.model.Credentials): TemporaryCredentials = new TemporaryCredentials(
    accessKeyId = c.getAccessKeyId,
    secretAccessKey = c.getSecretAccessKey,
    sessionToken = c.getSessionToken,
    expiration = new DateTime(c.getExpiration)
  )
}

case class TemporaryCredentials(
    accessKeyId: String,
    secretAccessKey: String,
    sessionToken: String,
    expiration: DateTime) extends aws.model.Credentials {

  setAccessKeyId(accessKeyId)
  setExpiration(expiration.toDate)
  setSecretAccessKey(secretAccessKey)
  setSessionToken(sessionToken)
}
