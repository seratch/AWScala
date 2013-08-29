package awscala.redshift

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ redshift => aws }

object Cluster {
  def apply(c: aws.model.Cluster): Cluster = new Cluster(
    identifier = c.getClusterIdentifier,
    dbName = c.getDBName,
    endpoint = Endpoint(c.getEndpoint),
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

class Cluster(
    val identifier: String,
    val dbName: String,
    val endpoint: Endpoint,
    val masterUserName: String,
    val status: String,
    val version: ClusterVersion,
    val nodeType: NodeType,
    val numOfNodes: Int,
    val modifyStatus: String,
    val availabilityZone: AvailabilityZone,
    val encrypted: Boolean,
    val allowVersionUpgrade: Boolean,
    val publiclyAccessible: Boolean,
    val automatedSnapshotRetentionPeriod: Int,
    val subnetGroupName: String,
    val restoreStatus: Option[RestoreStatus],
    val preferredMaintenanceWindow: String,
    val pendingModifiedValues: PendingModifiedValues,
    val parameterGroupStatuses: Seq[ClusterParameterGroupStatus],
    val securityGroupMemberships: Seq[ClusterSecurityGroupMembership],
    val vpcId: String,
    val vpcSecurityGroupMemberships: Seq[VpcSecurityGroupMembership],
    val createdAt: DateTime) extends aws.model.Cluster {

  setAllowVersionUpgrade(allowVersionUpgrade)
  setAutomatedSnapshotRetentionPeriod(automatedSnapshotRetentionPeriod)
  setAvailabilityZone(availabilityZone.name)
  setClusterCreateTime(createdAt.toDate)
  setClusterIdentifier(identifier)
  setClusterParameterGroups(parameterGroupStatuses.map(_.asInstanceOf[aws.model.ClusterParameterGroupStatus]).asJava)
  setClusterSecurityGroups(securityGroupMemberships.map(_.asInstanceOf[aws.model.ClusterSecurityGroupMembership]).asJava)
  setClusterStatus(status)
  setClusterSubnetGroupName(subnetGroupName)
  setClusterVersion(version.version)
  setDBName(dbName)
  setEncrypted(encrypted)
  setEndpoint(endpoint)
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

  def jdbcUrl: String = s"jdbc:postgresql://${endpoint.address}:${endpoint.port}/${dbName}"

}
