package awscala

trait CredentialsProvider extends com.amazonaws.auth.AWSCredentialsProvider {
  override def getCredentials: Credentials
  override def refresh: Unit
}
