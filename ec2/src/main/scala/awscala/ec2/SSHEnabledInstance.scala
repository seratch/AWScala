package awscala.ec2

import com.decodified.scalassh._
import java.io.File

case class SSHEnabledInstance(instance: Instance) {

  def ssh[T](f: SshClient => SSH.Result[T], keyPairFilePath: String): SSH.Result[T] = ssh(f, new File(keyPairFilePath))

  def ssh[T](f: SshClient => SSH.Result[T], keyPairFile: File): SSH.Result[T] = {
    instance.withKeyPair(keyPairFile)(_.ssh(f))
  }

}
