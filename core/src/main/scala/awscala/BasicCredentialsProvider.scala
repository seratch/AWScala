package awscala

class BasicCredentialsProvider(accessKey: String, secretKey: String) extends CredentialsProvider {
  private val credentials: Credentials = Credentials(accessKey, secretKey)
  override def getCredentials: Credentials = credentials
  override def refresh: Unit = {}
}

object BasicCredentialsProvider {
  def apply(accessKey: String, secretKey: String): BasicCredentialsProvider =
    new BasicCredentialsProvider(accessKey, secretKey)

  def apply(credentials: Credentials): BasicCredentialsProvider =
    new BasicCredentialsProvider(credentials.getAWSAccessKeyId, credentials.getAWSSecretKey)
}
