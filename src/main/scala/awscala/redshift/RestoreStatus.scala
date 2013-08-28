package awscala.redshift

import com.amazonaws.services.{ redshift => aws }

object RestoreStatus {
  def apply(r: aws.model.RestoreStatus): Option[RestoreStatus] = Option(r).map(r => new RestoreStatus(
    status = r.getStatus,
    currentRestoreRateInMegaBytesPerSecond = r.getCurrentRestoreRateInMegaBytesPerSecond,
    elapsedTimeInSeconds = r.getElapsedTimeInSeconds,
    estimatedTimeToCompletionInSeconds = r.getEstimatedTimeToCompletionInSeconds,
    progressInMegaBytes = r.getProgressInMegaBytes,
    snapshotSizeInMegaBytes = r.getSnapshotSizeInMegaBytes
  ))
}
case class RestoreStatus(
    status: String,
    currentRestoreRateInMegaBytesPerSecond: Double,
    elapsedTimeInSeconds: Long,
    estimatedTimeToCompletionInSeconds: Long,
    progressInMegaBytes: Long,
    snapshotSizeInMegaBytes: Long) extends aws.model.RestoreStatus {

  setCurrentRestoreRateInMegaBytesPerSecond(currentRestoreRateInMegaBytesPerSecond)
  setElapsedTimeInSeconds(elapsedTimeInSeconds)
  setEstimatedTimeToCompletionInSeconds(estimatedTimeToCompletionInSeconds)
  setProgressInMegaBytes(progressInMegaBytes)
  setSnapshotSizeInMegaBytes(snapshotSizeInMegaBytes)
  setStatus(status)
}

