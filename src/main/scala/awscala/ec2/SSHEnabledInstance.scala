package awscala.ec2

import com.decodified.scalassh._
import java.io.File

case class SSHEnabledInstance(instance: Instance) extends {

  def ssh[T](f: SshClient => SSH.Result[T], keyPairFilePath: String): Unit = ssh(f, new File(keyPairFilePath))

  def ssh[T](f: SshClient => SSH.Result[T], keyPairFile: File): Unit = {
    instance.withKeyPair(keyPairFile)(_.ssh(f))
  }

}
