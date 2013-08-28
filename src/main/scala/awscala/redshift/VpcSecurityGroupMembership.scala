package awscala.redshift

import com.amazonaws.services.{ redshift => aws }

object VpcSecurityGroupMembership {
  def apply(m: aws.model.VpcSecurityGroupMembership): VpcSecurityGroupMembership = new VpcSecurityGroupMembership(
    status = m.getStatus,
    vpcSecurityGroupId = m.getVpcSecurityGroupId
  )
}
case class VpcSecurityGroupMembership(status: String, vpcSecurityGroupId: String) extends aws.model.VpcSecurityGroupMembership {
  setStatus(status)
  setVpcSecurityGroupId(vpcSecurityGroupId)
}

