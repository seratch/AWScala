package awscala.redshift

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ redshift => aws }

object Redshift {

  def apply(credentials: Credentials = Credentials.defaultEnv): Redshift = new RedshiftClient(credentials)
  def apply(accessKeyId: String, secretAccessKey: String): Redshift = apply(Credentials(accessKeyId, secretAccessKey))

  def at(region: Region): Redshift = apply().at(region)
}

/**
 * Amazon Redshift Java client wrapper
 * @see "http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/"
 */
trait Redshift extends aws.AmazonRedshift {

  // TODO Snapshot, Event, describe others

  def at(region: Region): Redshift = {
    this.setRegion(region)
    this
  }

  // ------------------------------------------
  // Clusters
  // ------------------------------------------

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
      .withClusterVersion(newCluster.clusterVersion.value)
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

  def clusters: Seq[Cluster] = describeClusters.getClusters.asScala.map(c => Cluster(c))

  def cluster(identifier: String, marker: Option[String] = None, maxRecords: Option[Int] = None): Option[Cluster] = {
    val req = new aws.model.DescribeClustersRequest().withClusterIdentifier(identifier)
    marker.foreach(m => req.setMarker(m))
    maxRecords.foreach(mr => req.setMaxRecords(mr))
    describeClusters(req).getClusters.asScala.headOption.map(c => Cluster(c))
  }

  def delete(cluster: Cluster, finalSnapshotIdentifier: String, skipFinalSnapshot: Boolean = false): Unit = {
    deleteCluster(cluster, skipFinalSnapshot, finalSnapshotIdentifier)
  }
  def deleteCluster(cluster: Cluster, skipFinalSnapshot: Boolean = true, finalSnapshotIdentifier: String): Unit = {
    deleteCluster(new aws.model.DeleteClusterRequest()
      .withClusterIdentifier(cluster.identifier)
      .withFinalClusterSnapshotIdentifier(finalSnapshotIdentifier)
      .withSkipFinalClusterSnapshot(skipFinalSnapshot))
  }

  // ------------------------------------------
  // Cluster Parameter Groups
  // ------------------------------------------

  def createClusterParameterGroup(name: String, family: String, description: String): ClusterParameterGroup = {
    ClusterParameterGroup(
      createClusterParameterGroup(new aws.model.CreateClusterParameterGroupRequest()
        .withParameterGroupName(name)
        .withParameterGroupFamily(family)
        .withDescription(description))
    )
  }

  def delete(group: ClusterParameterGroup): Unit = deleteClusterParameterGroup(group)
  def deleteClusterParameterGroup(group: ClusterParameterGroup): Unit = {
    deleteClusterParameterGroup(new aws.model.DeleteClusterParameterGroupRequest().withParameterGroupName(group.name))
  }

  // ------------------------------------------
  // Cluster Security Groups
  // ------------------------------------------

  def createClusterSecurityGroup(name: String, description: String): ClusterSecurityGroup = {
    ClusterSecurityGroup(
      createClusterSecurityGroup(new aws.model.CreateClusterSecurityGroupRequest()
        .withClusterSecurityGroupName(name)
        .withDescription(description)
      )
    )
  }

  def delete(group: ClusterSecurityGroup): Unit = deleteClusterSecurityGroup(group)
  def deleteClusterSecurityGroup(group: ClusterSecurityGroup): Unit = {
    deleteClusterSecurityGroup(new aws.model.DeleteClusterSecurityGroupRequest().withClusterSecurityGroupName(group.name))
  }

}

/**
 * Default Implementation
 *
 * @param credentials credentials
 */
class RedshiftClient(credentials: Credentials = Credentials.defaultEnv)
  extends aws.AmazonRedshiftClient(credentials)
  with Redshift

