package awscala

package object ec2 {

  type InstanceType = com.amazonaws.services.ec2.model.InstanceType

  implicit def fromInstanceToSSHEnabledInstance(instance: Instance): SSHEnabledInstance = SSHEnabledInstance(instance)

}

