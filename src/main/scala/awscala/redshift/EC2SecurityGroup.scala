package awscala.redshift

import com.amazonaws.services.{ redshift => aws }

case class EC2SecurityGroup(name: String, ownerId: String, status: String) extends aws.model.EC2SecurityGroup {
  setEC2SecurityGroupName(name)
  setEC2SecurityGroupOwnerId(ownerId)
  setStatus(status)
}

