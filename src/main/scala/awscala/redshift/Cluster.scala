package awscala.redshift

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ redshift => aws }

object Cluster {
  def apply(c: aws.model.Cluster): Cluster = new Cluster(
    identifier = c.getClusterIdentifier,
    dbName = c.getDBName,
    masterUserName = c.getMasterUsername,
    status = c.getClusterStatus,
    version = ClusterVersion(c.getClusterVersion),
    nodeType = NodeType(c.getNodeType),
    numOfNodes = c.getNumberOfNodes,
    modifyStatus = c.getModifyStatus,
    availabilityZone = AvailabilityZone(c.getAvailabilityZone),
    encrypted = c.isEncrypted,
    allowVersionUpgrade = c.isAllowVersionUpgrade,
    publiclyAccessible = c.isPubliclyAccessible,
    automatedSnapshotRetentionPeriod = c.getAutomatedSnapshotRetentionPeriod,
    subnetGroupName = c.getClusterSubnetGroupName,
    restoreStatus = RestoreStatus(c.getRestoreStatus),
    preferredMaintenanceWindow = c.getPreferredMaintenanceWindow,
    pendingModifiedValues = PendingModifiedValues(c.getPendingModifiedValues),
    parameterGroupStatuses = c.getClusterParameterGroups.asScala.map(p => ClusterParameterGroupStatus(p)),
    securityGroupMemberships = c.getClusterSecurityGroups.asScala.map(m => ClusterSecurityGroupMembership(m)),
    vpcId = c.getVpcId,
    vpcSecurityGroupMemberships = c.getVpcSecurityGroups.asScala.map(m => VpcSecurityGroupMembership(m)),
    createdAt = new DateTime(c.getClusterCreateTime)
  )
}
case class Cluster(
    identifier: String,
    dbName: String,
    masterUserName: String,
    status: String,
    version: ClusterVersion,
    nodeType: NodeType,
    numOfNodes: Int,
    modifyStatus: String,
    availabilityZone: AvailabilityZone,
    encrypted: Boolean,
    allowVersionUpgrade: Boolean,
    publiclyAccessible: Boolean,
    automatedSnapshotRetentionPeriod: Int,
    subnetGroupName: String,
    restoreStatus: Option[RestoreStatus],
    preferredMaintenanceWindow: String,
    pendingModifiedValues: PendingModifiedValues,
    parameterGroupStatuses: Seq[ClusterParameterGroupStatus],
    securityGroupMemberships: Seq[ClusterSecurityGroupMembership],
    vpcId: String,
    vpcSecurityGroupMemberships: Seq[VpcSecurityGroupMembership],
    createdAt: DateTime) extends aws.model.Cluster {

  setAllowVersionUpgrade(allowVersionUpgrade)
  setAutomatedSnapshotRetentionPeriod(automatedSnapshotRetentionPeriod)
  setAvailabilityZone(availabilityZone.name)
  setClusterCreateTime(createdAt.toDate)
  setClusterIdentifier(identifier)
  setClusterParameterGroups(parameterGroupStatuses.map(_.asInstanceOf[aws.model.ClusterParameterGroupStatus]).asJava)
  setClusterSecurityGroups(securityGroupMemberships.map(_.asInstanceOf[aws.model.ClusterSecurityGroupMembership]).asJava)
  setClusterStatus(status)
  setClusterSubnetGroupName(subnetGroupName)
  setClusterVersion(version.value)
  setDBName(dbName)
  setEncrypted(encrypted)
  setMasterUsername(masterUserName)
  setModifyStatus(modifyStatus)
  setNodeType(nodeType.value)
  setNumberOfNodes(numOfNodes)
  setPendingModifiedValues(pendingModifiedValues)
  setPreferredMaintenanceWindow(preferredMaintenanceWindow)
  setPubliclyAccessible(publiclyAccessible)
  setRestoreStatus(restoreStatus.orNull[aws.model.RestoreStatus])
  setVpcId(vpcId)
  setVpcSecurityGroups(vpcSecurityGroupMemberships.map(_.asInstanceOf[aws.model.VpcSecurityGroupMembership]).asJava)
}
