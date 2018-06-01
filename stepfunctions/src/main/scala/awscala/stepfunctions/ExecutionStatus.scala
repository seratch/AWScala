package awscala.stepfunctions

object ExecutionStatus {
  sealed trait ExecutionStatus
  case object NotStarted extends ExecutionStatus
  case object Running extends ExecutionStatus
  case object Succeeded extends ExecutionStatus
  case object Failed extends ExecutionStatus
  case object TimedOut extends ExecutionStatus
  case object Aborted extends ExecutionStatus

  def fromString(status: String): ExecutionStatus = status.toLowerCase match {
    case "running" => Running
    case "succeeded" => Succeeded
    case "failed" => Failed
    case "timed_out" => TimedOut
    case "aborted" => TimedOut
  }
}
