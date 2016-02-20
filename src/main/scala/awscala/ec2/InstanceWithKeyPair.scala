package awscala.ec2

import awscala._
import com.amazonaws.services.{ ec2 => aws }

case class InstanceWithKeyPair(
    override val underlying: aws.model.Instance,
    keyPairFile: File,
    user: String,
    connectionTimeout: Int
) extends Instance(underlying) {

  import com.decodified.scalassh._

  def ssh[T](f: SshClient => SSH.Result[T]) = SSH[T](publicDnsName, provider(keyPairFile))(f)

  private[this] def provider(keyPairFile: File): HostConfigProvider = new FromStringsHostConfigProvider {
    def rawLines(host: String): com.decodified.scalassh.Validated[(String, TraversableOnce[String])] =
      if (keyPairFile.exists()) {
        Right("dummy_source" -> (
          Seq(
            "login-type = keyfile",
            s"username = $user",
            s"keyfile = ${keyPairFile.getAbsolutePath}",
            s"command-timeout = $connectionTimeout",
            "fingerprint = any" //TODO: ask if user will trust any host key provided by the server. Currently it's always YES.
          )
        ))
      } else {
        Left(s"KeyFile ${keyPairFile.getAbsolutePath} does not exist")
      }
  }

}
