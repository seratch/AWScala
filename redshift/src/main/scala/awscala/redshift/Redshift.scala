package awscala.redshift

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.{ redshift => aws }

object Redshift {

  def apply(credentials: Credentials)(implicit region: Region): Redshift = new RedshiftClient(BasicCredentialsProvider(credentials.getAWSAccessKeyId, credentials.getAWSSecretKey)).at(region)

  def apply(credentialsProvider: AWSCredentialsProvider = CredentialsLoader.load())(implicit region: Region = Region.default()): Redshift = new RedshiftClient(credentialsProvider).at(region)

  def apply(accessKeyId: String, secretAccessKey: String)(implicit region: Region): Redshift = apply(BasicCredentialsProvider(accessKeyId, secretAccessKey)).at(region)

  def at(region: Region): Redshift = apply()(region)
}

/**
 * Amazon Redshift Java client wrapper
 * @see [[http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/]]
 */
trait Redshift extends aws.AmazonRedshift {

  def at(region: Region): Redshift = {
    this.setRegion(region)
    this
  }

  // ------------------------------------------
  // Clusters
  // ------------------------------------------

  def clusters: Seq[Cluster] = describeClusters.getClusters.asScala.map(c => Cluster(c))

  def cluster(identifier: String, marker: Option[String] = None, maxRecords: Option[Int] = None): Option[Cluster] = {
    val req = new aws.model.DescribeClustersRequest().withClusterIdentifier(identifier)
    marker.foreach(m => req.setMarker(m))
    maxRecords.foreach(mr => req.setMaxRecords(mr))
    describeClusters(req).getClusters.asScala.headOption.map(c => Cluster(c))
  }

  def createCluster(newCluster: NewCluster): Cluster = {

    val req = new aws.model.CreateClusterRequest()
      .withAllowVersionUpgrade(newCluster.allowVersionUpgrade)
      .withAutomatedSnapshotRetentionPeriod(newCluster.automatedSnapshotRetentionPeriod)
      .withAvailabilityZone(newCluster.availabilityZone.map(_.name).orNull[String])
      .withClusterIdentifier(newCluster.identifier)
      .withClusterParameterGroupName(newCluster.parameterGroupName.orNull[String])
      .withClusterSecurityGroups(newCluster.securityGroupNames.asJava)
      .withClusterSubnetGroupName(newCluster.subnetGroupName.orNull[String])
      .withClusterType(newCluster.clusterType.name)
      .withClusterVersion(newCluster.clusterVersion.version)
      .withDBName(newCluster.dbName)
      .withEncrypted(newCluster.encrypted)
      .withMasterUsername(newCluster.masterUsername)
      .withMasterUserPassword(newCluster.masterUserPassword)
      .withNodeType(newCluster.nodeType.value)
      .withPort(newCluster.port)
      .withPreferredMaintenanceWindow(newCluster.preferredMaintenanceWindow.orNull[String])
      .withPubliclyAccessible(newCluster.publiclyAccessible)
      .withVpcSecurityGroupIds(newCluster.vpcSecurityGroupIds.asJava)

    if (newCluster.clusterType == ClusterType.MultiNode) {
      req.setNumberOfNodes(newCluster.numOfNodes)
    }
    Cluster(createCluster(req))
  }

  def delete(cluster: Cluster, finalSnapshotIdentifier: String): Unit = {
    deleteCluster(cluster, finalSnapshotIdentifier)
  }
  def deleteCluster(cluster: Cluster, finalSnapshotIdentifier: String): Unit = {
    deleteCluster(new aws.model.DeleteClusterRequest()
      .withClusterIdentifier(cluster.identifier)
      .withFinalClusterSnapshotIdentifier(finalSnapshotIdentifier)
      .withSkipFinalClusterSnapshot(false))
  }
  def deleteWithoutFinalSnapshot(cluster: Cluster): Unit = deleteClusterWithoutFinalSnapshot(cluster)
  def deleteClusterWithoutFinalSnapshot(cluster: Cluster): Unit = {
    deleteCluster(new aws.model.DeleteClusterRequest()
      .withClusterIdentifier(cluster.identifier)
      .withSkipFinalClusterSnapshot(true))
  }

  def clusterVersions: Seq[ClusterVersion] = describeClusterVersions.getClusterVersions.asScala.map(v => ClusterVersion(v))

  def clusterVersion(version: String, paramtereGroupFamily: String = null, marker: String = null, maxRecords: Int = -1): Option[ClusterVersion] = {
    val req = new aws.model.DescribeClusterVersionsRequest().withClusterVersion(version)
    if (marker != null) {
      req.setMarker(marker)
    }
    if (maxRecords != -1) {
      req.setMaxRecords(maxRecords)
    }
    if (paramtereGroupFamily != null) {
      req.setClusterParameterGroupFamily(paramtereGroupFamily)
    }
    describeClusterVersions(req).getClusterVersions.asScala.headOption.map(v => ClusterVersion(v))
  }

  def reservedNodes: Seq[ReservedNode] = describeReservedNodes.getReservedNodes.asScala.map(n => ReservedNode(n)).toSeq

  // ------------------------------------------
  // Snapshots
  // ------------------------------------------

  def snapshots: Seq[Snapshot] = {
    describeClusterSnapshots(new aws.model.DescribeClusterSnapshotsRequest())
      .getSnapshots.asScala.map(s => Snapshot(s))
  }

  def snapshot(
    snapshotIdentifier: String,
    clusterIdentifier: String = null,
    from: DateTime = null,
    to: DateTime = null,
    marker: String = null,
    maxRecords: Int = -1,
    ownerAccount: String = null,
    snapshotType: SnapshotType = null): Option[Snapshot] = {

    val req = new aws.model.DescribeClusterSnapshotsRequest()
      .withSnapshotIdentifier(snapshotIdentifier)
      .withStartTime(from.toDate)
      .withMarker(marker)
      .withMaxRecords(maxRecords)
      .withOwnerAccount(ownerAccount)
      .withSnapshotType(snapshotType.value)

    if (clusterIdentifier != null) {
      req.setClusterIdentifier(clusterIdentifier)
    }
    if (from != null) {
      req.setStartTime(from.toDate)
    }
    if (to != null) {
      req.setEndTime(to.toDate)
    }
    if (marker != null) {
      req.setMarker(marker)
    }
    if (maxRecords != -1) {
      req.setMaxRecords(maxRecords)
    }
    if (ownerAccount != null) {
      req.setOwnerAccount(ownerAccount)
    }
    if (snapshotType != null) {
      req.setSnapshotType(snapshotType.value)
    }
    describeClusterSnapshots(req).getSnapshots.asScala.headOption.map(s => Snapshot(s))
  }

  def createSnapshot(cluster: Cluster, snapshotIdentifier: String): Snapshot = {
    Snapshot(createClusterSnapshot(new aws.model.CreateClusterSnapshotRequest()
      .withClusterIdentifier(cluster.identifier)
      .withSnapshotIdentifier(snapshotIdentifier)))
  }

  def authorizeAccess(snapshot: Snapshot, accountId: String): Unit = authorizeSnapshotAccess(snapshot, accountId)
  def authorizeSnapshotAccess(snapshot: Snapshot, accountId: String): Unit = {
    authorizeSnapshotAccess(new aws.model.AuthorizeSnapshotAccessRequest()
      .withAccountWithRestoreAccess(accountId)
      .withSnapshotClusterIdentifier(snapshot.clusterIdentifier)
      .withSnapshotIdentifier(snapshot.snapshotIdentifier))
  }

  def revokeAccess(snapshot: Snapshot, accountId: String): Unit = revokeSnapshotAccess(snapshot, accountId)
  def revokeSnapshotAccess(snapshot: Snapshot, accountId: String): Unit = {
    revokeSnapshotAccess(new aws.model.RevokeSnapshotAccessRequest()
      .withAccountWithRestoreAccess(accountId)
      .withSnapshotClusterIdentifier(snapshot.clusterIdentifier)
      .withSnapshotIdentifier(snapshot.snapshotIdentifier))
  }

  def copy(source: Snapshot, targetIdentifier: String): Snapshot = copySnapshot(source, targetIdentifier)
  def copySnapshot(source: Snapshot, targetIdentifier: String): Snapshot = {
    Snapshot(copyClusterSnapshot(new aws.model.CopyClusterSnapshotRequest()
      .withTargetSnapshotIdentifier(targetIdentifier)
      .withSourceSnapshotClusterIdentifier(source.clusterIdentifier)
      .withSourceSnapshotIdentifier(source.snapshotIdentifier)))
  }

  def delete(snapshot: Snapshot): Unit = deleteSnapshot(snapshot)
  def deleteSnapshot(snapshot: Snapshot): Unit = {
    deleteClusterSnapshot(new aws.model.DeleteClusterSnapshotRequest()
      .withSnapshotClusterIdentifier(snapshot.clusterIdentifier)
      .withSnapshotIdentifier(snapshot.snapshotIdentifier))
  }

  // ------------------------------------------
  // Events
  // ------------------------------------------

  def events: Seq[Event] = describeEvents.getEvents.asScala.map(e => Event(e)).toSeq

  // ------------------------------------------
  // Cluster Parameter Groups
  // ------------------------------------------

  def clusterParameterGroups: Seq[ClusterParameterGroup] = {
    describeClusterParameterGroups(new aws.model.DescribeClusterParameterGroupsRequest())
      .getParameterGroups.asScala.map(g => ClusterParameterGroup(g))
  }

  def clusterParameterGroup(name: String, marker: String = null, maxRecords: Int = -1): Option[ClusterParameterGroup] = {
    val req = new aws.model.DescribeClusterParameterGroupsRequest().withParameterGroupName(name)
    if (marker != null) {
      req.setMarker(marker)
    }
    if (maxRecords != -1) {
      req.setMaxRecords(maxRecords)
    }
    describeClusterParameterGroups(req).getParameterGroups.asScala.headOption.map(g => ClusterParameterGroup(g))
  }

  def createClusterParameterGroup(name: String, family: String, description: String): ClusterParameterGroup = {
    ClusterParameterGroup(
      createClusterParameterGroup(new aws.model.CreateClusterParameterGroupRequest()
        .withParameterGroupName(name)
        .withParameterGroupFamily(family)
        .withDescription(description)))
  }

  def delete(group: ClusterParameterGroup): Unit = deleteClusterParameterGroup(group)

  def deleteClusterParameterGroup(group: ClusterParameterGroup): Unit = {
    deleteClusterParameterGroup(new aws.model.DeleteClusterParameterGroupRequest().withParameterGroupName(group.name))
  }

  // ------------------------------------------
  // Cluster Security Groups
  // ------------------------------------------

  def authorizeSecurityGroupIngress(securityGroup: ClusterSecurityGroup, ec2SecurityGroup: EC2SecurityGroup, cidrip: String): Unit = {
    authorizeClusterSecurityGroupIngress(new aws.model.AuthorizeClusterSecurityGroupIngressRequest()
      .withCIDRIP(cidrip)
      .withClusterSecurityGroupName(securityGroup.name)
      .withEC2SecurityGroupName(ec2SecurityGroup.name)
      .withEC2SecurityGroupOwnerId(ec2SecurityGroup.ownerId))
  }

  def clusterSecurityGroups: Seq[ClusterSecurityGroup] = {
    describeClusterSecurityGroups(new aws.model.DescribeClusterSecurityGroupsRequest())
      .getClusterSecurityGroups.asScala.map(g => ClusterSecurityGroup(g))
  }

  def clusterSecurityGroup(name: String, marker: String = null, maxRecords: Int = -1): Option[ClusterSecurityGroup] = {
    val req = new aws.model.DescribeClusterSecurityGroupsRequest().withClusterSecurityGroupName(name)
    if (marker != null) {
      req.setMarker(marker)
    }
    if (maxRecords != -1) {
      req.setMaxRecords(maxRecords)
    }
    describeClusterSecurityGroups(req).getClusterSecurityGroups.asScala.headOption.map(g => ClusterSecurityGroup(g))
  }

  def createClusterSecurityGroup(name: String, description: String): ClusterSecurityGroup = {
    ClusterSecurityGroup(
      createClusterSecurityGroup(new aws.model.CreateClusterSecurityGroupRequest()
        .withClusterSecurityGroupName(name)
        .withDescription(description)))
  }

  def delete(group: ClusterSecurityGroup): Unit = deleteClusterSecurityGroup(group)

  def deleteClusterSecurityGroup(group: ClusterSecurityGroup): Unit = {
    deleteClusterSecurityGroup(new aws.model.DeleteClusterSecurityGroupRequest().withClusterSecurityGroupName(group.name))
  }

  // ------------------------------------------
  // Cluster Subnet Groups
  // ------------------------------------------

  def clusterSubnetGroups: Seq[ClusterSubnetGroup] = {
    describeClusterSubnetGroups(new aws.model.DescribeClusterSubnetGroupsRequest())
      .getClusterSubnetGroups.asScala.map(g => ClusterSubnetGroup(g))
  }

  def clusterSubnetGroup(name: String, marker: String = null, maxRecords: Int = -1): Option[ClusterSubnetGroup] = {
    val req = new aws.model.DescribeClusterSubnetGroupsRequest().withClusterSubnetGroupName(name)
    if (marker != null) {
      req.setMarker(marker)
    }
    if (maxRecords != -1) {
      req.setMaxRecords(maxRecords)
    }
    describeClusterSubnetGroups(req).getClusterSubnetGroups.asScala.headOption.map(g => ClusterSubnetGroup(g))
  }

  def createSubnetGroup(name: String, description: String, subnetIds: Seq[String]): ClusterSubnetGroup = {
    ClusterSubnetGroup(createClusterSubnetGroup(
      new aws.model.CreateClusterSubnetGroupRequest()
        .withClusterSubnetGroupName(name)
        .withDescription(description)
        .withSubnetIds(subnetIds.asJava)))
  }

  def delete(group: ClusterSubnetGroup): Unit = deleteSubnetGroup(group)
  def deleteSubnetGroup(group: ClusterSubnetGroup): Unit = {
    deleteClusterSubnetGroup(new aws.model.DeleteClusterSubnetGroupRequest()
      .withClusterSubnetGroupName(group.name))
  }

}

/**
 * Default Implementation
 *
 * @param credentialsProvider credentialsProvider
 */
class RedshiftClient(credentialsProvider: AWSCredentialsProvider = CredentialsLoader.load())
  extends aws.AmazonRedshiftClient(credentialsProvider)
  with Redshift

