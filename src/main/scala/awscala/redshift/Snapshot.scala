package awscala.redshift

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ redshift => aws }

object Snapshot {

  def apply(s: aws.model.Snapshot): Snapshot = new Snapshot(
    snapshotIdentifier = s.getSnapshotIdentifier,
    clusterIdentifier = s.getClusterIdentifier,
    clusterVersion = ClusterVersion(s.getClusterVersion),
    dbName = s.getDBName,
    port = s.getPort,
    masterUsername = s.getMasterUsername,
    status = Status(s.getStatus),
    availabilityZone = AvailabilityZone(s.getAvailabilityZone),
    snapshotType = SnapshotType(s.getSnapshotType),
    nodeType = NodeType(s.getNodeType),
    numOfNodes = s.getNumberOfNodes,
    ownerAccount = s.getOwnerAccount,
    encrypted = s.isEncrypted,
    elapsedTimeInSeconds = s.getElapsedTimeInSeconds,
    estimatedSecondsToCompletion = s.getEstimatedSecondsToCompletion,
    actualIncrementalBackupSizeInMegaBytes = s.getActualIncrementalBackupSizeInMegaBytes,
    currentBackupRateInMegaBytesPerSecond = s.getCurrentBackupRateInMegaBytesPerSecond,
    backupProgressInMegaBytes = s.getBackupProgressInMegaBytes,
    totalBackupSizeInMegaBytes = s.getTotalBackupSizeInMegaBytes,
    vpcId = s.getVpcId,
    accountsWithRestoreAccess = s.getAccountsWithRestoreAccess.asScala.map(a => AccountWithRestoreAccess(a.getAccountId)).toSeq,
    clusterCreatedAt = new DateTime(s.getClusterCreateTime),
    snapshotCreatedAt = new DateTime(s.getSnapshotCreateTime)
  )
}

class Snapshot(
    val snapshotIdentifier: String,
    val clusterIdentifier: String,
    val clusterVersion: ClusterVersion,
    val dbName: String,
    val port: Int,
    val masterUsername: String,
    val status: Status,
    val availabilityZone: AvailabilityZone,
    val snapshotType: SnapshotType,
    val nodeType: NodeType,
    val numOfNodes: Int,
    val ownerAccount: String,
    val encrypted: Boolean,
    val elapsedTimeInSeconds: Long,
    val estimatedSecondsToCompletion: Long,
    val actualIncrementalBackupSizeInMegaBytes: Double,
    val currentBackupRateInMegaBytesPerSecond: Double,
    val backupProgressInMegaBytes: Double,
    val totalBackupSizeInMegaBytes: Double,
    val vpcId: String,
    val accountsWithRestoreAccess: Seq[AccountWithRestoreAccess],
    val clusterCreatedAt: DateTime,
    val snapshotCreatedAt: DateTime) extends aws.model.Snapshot {

  setAccountsWithRestoreAccess(accountsWithRestoreAccess.map(_.asInstanceOf[aws.model.AccountWithRestoreAccess]).asJava)
  setActualIncrementalBackupSizeInMegaBytes(actualIncrementalBackupSizeInMegaBytes)
  setAvailabilityZone(availabilityZone.name)
  setBackupProgressInMegaBytes(backupProgressInMegaBytes)
  setClusterCreateTime(clusterCreatedAt.toDate)
  setClusterIdentifier(clusterIdentifier)
  setClusterVersion(clusterVersion.version)
  setCurrentBackupRateInMegaBytesPerSecond(currentBackupRateInMegaBytesPerSecond)
  setDBName(dbName)
  setElapsedTimeInSeconds(elapsedTimeInSeconds)
  setEncrypted(encrypted)
  setEstimatedSecondsToCompletion(estimatedSecondsToCompletion)
  setMasterUsername(masterUsername)
  setNodeType(nodeType.value)
  setNumberOfNodes(numOfNodes)
  setOwnerAccount(ownerAccount)
  setPort(port)
  setSnapshotCreateTime(snapshotCreatedAt.toDate)
  setSnapshotIdentifier(snapshotIdentifier)
  setSnapshotType(snapshotType.value)
  setStatus(status.value)
  setTotalBackupSizeInMegaBytes(totalBackupSizeInMegaBytes)
  setVpcId(vpcId)

  def destroy()(implicit redshift: Redshift) = redshift.delete(this)
}
