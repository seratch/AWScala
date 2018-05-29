package awscala.redshift

import com.amazonaws.services.{ redshift => aws }

object ClusterParameterGroupStatus {
  def apply(s: aws.model.ClusterParameterGroupStatus): ClusterParameterGroupStatus = new ClusterParameterGroupStatus(
    applyStatus = s.getParameterApplyStatus,
    groupName = s.getParameterGroupName)
}
case class ClusterParameterGroupStatus(applyStatus: String, groupName: String) extends aws.model.ClusterParameterGroupStatus {
  setParameterApplyStatus(applyStatus)
  setParameterGroupName(groupName)
}
