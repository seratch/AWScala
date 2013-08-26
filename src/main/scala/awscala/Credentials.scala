package awscala

object Credentials {

  val AWS_ACCESS_KEY_ID = "AWS_ACCESS_KEY_ID"
  val AWS_SECRET_ACCESS_KEY = "AWS_SECRET_ACCESS_KEY"

  def defaultEnv: Credentials = {
    new Credentials(
      accessKeyId = Option(System.getenv(AWS_ACCESS_KEY_ID)).getOrElse {
        throw new IllegalStateException(s"'${AWS_ACCESS_KEY_ID}' env value was not found!")
      },
      secretAccessKey = Option(System.getenv(AWS_SECRET_ACCESS_KEY)).getOrElse {
        throw new IllegalStateException(s"'${AWS_SECRET_ACCESS_KEY}' env value was not found!")
      }
    )
  }

}

case class Credentials(accessKeyId: String, secretAccessKey: String) extends com.amazonaws.auth.AWSCredentials {

  override def getAWSAccessKeyId: String = accessKeyId
  override def getAWSSecretKey: String = secretAccessKey

}
