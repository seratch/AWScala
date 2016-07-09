package awscala

import com.amazonaws.auth._
import com.amazonaws.AmazonClientException

/**
 * AWS Credentials loader.
 */
object CredentialsLoader {

  def load(): CredentialsProvider = {
    val provider = DefaultCredentialsProvider()
    if (tryCredentials(provider)) {
      provider
    } else {
      throw new IllegalStateException(s"Failed to load AWS credentials! Make sure about environment or configuration.")
    }
  }

  private[this] def tryCredentials(provider: CredentialsProvider): Boolean = {
    try {
      provider.getCredentials
      true
    } catch {
      case e: AmazonClientException => false
    }
  }

}
