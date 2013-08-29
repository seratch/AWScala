package awscala.redshift

import com.amazonaws.services.{ redshift => aws }
import awscala._

object Subnet {

  def apply(s: aws.model.Subnet): Subnet = new Subnet(
    identifier = s.getSubnetIdentifier,
    availabilityZone = AvailabilityZone(s.getSubnetAvailabilityZone.getName),
    status = s.getSubnetStatus
  )
}

case class Subnet(identifier: String, availabilityZone: AvailabilityZone, status: String) extends aws.model.Subnet {

  setSubnetAvailabilityZone(new aws.model.AvailabilityZone().withName(availabilityZone.name))
  setSubnetIdentifier(identifier)
  setSubnetStatus(status)
}
