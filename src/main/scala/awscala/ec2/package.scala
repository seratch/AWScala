package awscala

import scala.language.implicitConversions

package object ec2 {
  // Workaround for https://issues.scala-lang.org/browse/SI-7139
  val InstanceType = InstanceType0
  type InstanceType = com.amazonaws.services.ec2.model.InstanceType

  implicit def fromInstanceToSSHEnabledInstance(instance: Instance): SSHEnabledInstance = SSHEnabledInstance(instance)

}

