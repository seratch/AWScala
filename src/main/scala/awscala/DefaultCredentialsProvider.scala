package awscala

class DefaultCredentialsProvider extends CredentialsProvider {
  private val provider = new com.amazonaws.auth.DefaultAWSCredentialsProviderChain
  provider.setReuseLastProvider(false)
  
  override def getCredentials: Credentials = {
    provider.getCredentials match {
      case sc: com.amazonaws.auth.AWSSessionCredentials => Credentials(sc.getAWSAccessKeyId, sc.getAWSSecretKey, sc.getSessionToken)
      case c => Credentials(c.getAWSAccessKeyId, c.getAWSSecretKey)
    }
  }
  override def refresh: Unit = provider.refresh
}

object DefaultCredentialsProvider {
  def apply(): DefaultCredentialsProvider =
    new DefaultCredentialsProvider
}
