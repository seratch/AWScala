package awscala.redshift

import awscala._

case class NewCluster(
  identifier: String,
  dbName: String,
  masterUsername: String,
  masterUserPassword: String,
  parameterGroupName: Option[String] = None,
  availabilityZone: Option[AvailabilityZone] = None,
  subnetGroupName: Option[String] = None,
  clusterType: ClusterType = ClusterType.SingleNode,
  clusterVersion: ClusterVersion = ClusterVersion.Version_1_0,
  nodeType: NodeType = NodeType.dw_hs1_xlarge,
  numOfNodes: Int = 1,
  port: Int = 5439,
  preferredMaintenanceWindow: Option[String] = None,
  encrypted: Boolean = false,
  allowVersionUpgrade: Boolean = true,
  publiclyAccessible: Boolean = true,
  automatedSnapshotRetentionPeriod: Int = 1,
  securityGroupNames: Seq[String] = Nil,
  vpcSecurityGroupIds: Seq[String] = Nil
)
