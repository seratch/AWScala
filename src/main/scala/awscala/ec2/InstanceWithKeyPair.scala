package awscala.ec2

import awscala._
import com.amazonaws.services.{ ec2 => aws }

case class InstanceWithKeyPair(private val underlying: aws.model.Instance, keyPairFile: File)
    extends Instance(underlying) {

  import com.decodified.scalassh._

  def ssh[T](f: SshClient => SSH.Result[T]) = SSH[T](publicDnsName, provider(keyPairFile))(f)

  private[this] def provider(keyPairFile: File): HostConfigProvider = new FromStringsHostConfigProvider {
    def rawLines(host: String): com.decodified.scalassh.Validated[(String, TraversableOnce[String])] =
      if (keyPairFile.exists()) {
        Right("dummy_source" -> (
          Seq(
            "login-type = keyfile",
            "username = ec2-user",
            s"keyfile = ${keyPairFile.getAbsolutePath}",
            "command-timeout = 30000",
            "fingerprint = any" //TODO: ask if user will trust any host key provided by the server. Currently it's always YES.
          )))
      } else {
        Left(s"KeyFile ${keyPairFile.getAbsolutePath} does not exist")
      }
  }

}
