package awscala.ec2

import awscala._
import com.amazonaws.services.{ ec2 => aws }
import com.decodified.scalassh.HostKeyVerifiers.DontVerify

import scala.util.{ Failure, Success, Try }

case class InstanceWithKeyPair(
  override val underlying: aws.model.Instance,
  keyPairFile: File,
  user: String,
  connectionTimeout: Int) extends Instance(underlying) {

  import com.decodified.scalassh._

  def ssh[T](f: SshClient => SSH.Result[T]): Try[T] = SSH[T](publicDnsName, provider(keyPairFile))(f)

  private[this] def provider(keyPairFile: File): HostConfigProvider = new HostConfigProvider {
    override def apply(v1: String): Try[HostConfig] =
      Success(HostConfig(
        login = PublicKeyLogin(user, None, List(keyPairFile.getAbsolutePath)),
        hostName = "",
        port = 22,
        connectTimeout = None,
        connectionTimeout = None,
        commandTimeout = Some(connectionTimeout),
        enableCompression = false,
        hostKeyVerifier = DontVerify,
        ptyConfig = None,
        sshjConfig = HostConfig.DefaultSshjConfig))
  }

}
