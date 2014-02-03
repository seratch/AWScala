package awscala

import com.amazonaws.auth._
import com.amazonaws.AmazonClientException

/**
 * AWS Credentials loader.
 */
object CredentialsLoader {

  def load(): Credentials = {
    try Credentials.defaultEnv
    catch {
      case e: IllegalStateException =>
        tryCredentials(new EnvironmentVariableCredentialsProvider)
          .orElse(tryCredentials(new SystemPropertiesCredentialsProvider))
          .orElse(tryCredentials(new InstanceProfileCredentialsProvider))
          .getOrElse { throw new IllegalStateException(s"Failed to load AWS credentials! Make sure about environment or configuration.") }
    }
  }

  private[this] def asScala(c: AWSCredentials): Credentials = Credentials(c.getAWSAccessKeyId, c.getAWSSecretKey)

  private[this] def tryCredentials(provider: AWSCredentialsProvider): Option[Credentials] = {
    try Option(asScala(provider.getCredentials))
    catch {
      case e: AmazonClientException => None
    }
  }

}
