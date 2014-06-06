package awscala

case class Credentials(accessKeyId: String, secretAccessKey: String) extends com.amazonaws.auth.AWSCredentials {

  override def getAWSAccessKeyId: String = accessKeyId
  override def getAWSSecretKey: String = secretAccessKey

}
