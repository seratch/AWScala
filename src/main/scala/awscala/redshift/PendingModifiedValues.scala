package awscala.redshift

import com.amazonaws.services.{ redshift => aws }

object PendingModifiedValues {
  def apply(p: aws.model.PendingModifiedValues): PendingModifiedValues = new PendingModifiedValues(
    clusterType = Option(p.getClusterType),
    clusterVersion = Option(p.getClusterVersion),
    masterUserPassword = Option(p.getMasterUserPassword),
    nodeType = Option(p.getNodeType),
    numOfNodes = Option[Integer](p.getNumberOfNodes).map(_.asInstanceOf[Int]),
    automatedSnapshotRetentionPeriod = Option[Integer](p.getAutomatedSnapshotRetentionPeriod).map(_.asInstanceOf[Int]))
}
case class PendingModifiedValues(
  clusterType: Option[String],
  clusterVersion: Option[String],
  masterUserPassword: Option[String],
  nodeType: Option[String],
  numOfNodes: Option[Int],
  automatedSnapshotRetentionPeriod: Option[Int]) extends aws.model.PendingModifiedValues {

  setAutomatedSnapshotRetentionPeriod(automatedSnapshotRetentionPeriod.map(_.asInstanceOf[Integer]).orNull[Integer])
  setClusterType(clusterType.orNull[String])
  setClusterVersion(clusterVersion.orNull[String])
  setMasterUserPassword(masterUserPassword.orNull[String])
  setNodeType(nodeType.orNull[String])
  setNumberOfNodes(numOfNodes.map(_.asInstanceOf[Integer]).orNull[Integer])
}

