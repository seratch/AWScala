package awscala.redshift

import com.amazonaws.services.{ redshift => aws }

object ClusterSecurityGroupMembership {
  def apply(m: aws.model.ClusterSecurityGroupMembership): ClusterSecurityGroupMembership = new ClusterSecurityGroupMembership(
    groupName = m.getClusterSecurityGroupName,
    status = m.getStatus
  )
}
case class ClusterSecurityGroupMembership(groupName: String, status: String) extends aws.model.ClusterSecurityGroupMembership {
  setClusterSecurityGroupName(groupName)
  setStatus(status)
}

