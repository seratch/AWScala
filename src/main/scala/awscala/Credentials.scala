package awscala

class Credentials(accessKeyId: String, secretAccessKey: String) extends com.amazonaws.auth.AWSCredentials {
  override def getAWSAccessKeyId: String = accessKeyId
  override def getAWSSecretKey: String = secretAccessKey
}

class SessionCredentials(accessKeyId: String, secretAccessKey: String, token: String)
  extends Credentials(accessKeyId, secretAccessKey) with com.amazonaws.auth.AWSSessionCredentials {
  override def getSessionToken: String = token
}

object Credentials {
  def apply(accessKeyId: String, secretAccessKey: String): Credentials =
    new Credentials(accessKeyId, secretAccessKey)

  def apply(accessKeyId: String, secretAccessKey: String, token: String): Credentials =
    new SessionCredentials(accessKeyId, secretAccessKey, token)
}
