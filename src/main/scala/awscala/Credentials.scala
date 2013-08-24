package awscala

object Credentials {

  def defaultEnv: Credentials = new Credentials(
    accessKeyId = System.getenv("AWS_ACCESS_KEY_ID"),
    secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY")
  )

}

case class Credentials(accessKeyId: String, secretAccessKey: String) extends com.amazonaws.auth.AWSCredentials {

  override def getAWSAccessKeyId: String = accessKeyId
  override def getAWSSecretKey: String = secretAccessKey

}
