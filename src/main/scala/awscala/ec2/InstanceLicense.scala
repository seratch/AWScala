package awscala.ec2

import com.amazonaws.services.{ ec2 => aws }

object InstanceLicense {

  def apply(instance: Instance, l: aws.model.InstanceLicense): InstanceLicense = InstanceLicense(
    instance = instance,
    pool = l.getPool
  )
}

case class InstanceLicense(instance: Instance, pool: String) extends aws.model.InstanceLicense {
  setPool(pool)
}
