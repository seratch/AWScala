package awscala.stepfunctions

import awscala.stepfunctions.ExecutionStatus.{ Aborted, ExecutionStatus, Failed, TimedOut }
import com.amazonaws.services.stepfunctions.model.HistoryEvent

object ExecutionEventDetails {
  sealed trait ExecutionEventDetails
  case class EventSucceeded(output: String) extends ExecutionEventDetails
  case class EventFailed(error: Option[String], cause: Option[String], failureType: ExecutionStatus) extends ExecutionEventDetails
  case class EventScheduled(input: String, resource: String, timeout: Option[Long], heartbeat: Option[Long])
    extends ExecutionEventDetails
  case class EventStarted(workerName: Option[String]) extends ExecutionEventDetails
  case class StateStarted(name: String) extends ExecutionEventDetails
  case class StateFailed(name: String, output: String, status: ExecutionStatus) extends ExecutionEventDetails
  case class StateSucceeded(name: String, output: String) extends ExecutionEventDetails

  def fromEvent(e: HistoryEvent): Option[ExecutionEventDetails] = Option {
    e.getType match {
      case "ActivityFailed" =>
        EventFailed(
          Option(e.getActivityFailedEventDetails.getError),
          Option(e.getActivityFailedEventDetails.getCause),
          Failed)
      case "ActivityScheduleFailed" =>
        EventFailed(
          Option(e.getActivityScheduleFailedEventDetails.getError),
          Option(e.getActivityScheduleFailedEventDetails.getCause),
          Failed)
      case "ActivityScheduled" =>
        EventScheduled(
          e.getActivityScheduledEventDetails.getInput,
          e.getActivityScheduledEventDetails.getResource,
          zeroAsNone(e.getActivityScheduledEventDetails.getTimeoutInSeconds),
          zeroAsNone(e.getActivityScheduledEventDetails.getHeartbeatInSeconds))
      case "ActivityStarted" => EventStarted(Option(e.getActivityStartedEventDetails.getWorkerName))
      case "ActivitySucceeded" => EventSucceeded(e.getActivitySucceededEventDetails.getOutput)
      case "ActivityTimedOut" =>
        EventFailed(
          Option(e.getActivityTimedOutEventDetails.getError),
          Option(e.getActivityTimedOutEventDetails.getCause),
          TimedOut)

      case "ChoiceStateEntered" => StateStarted(e.getStateEnteredEventDetails.getName)
      case "ChoiceStateExited" =>
        StateSucceeded(e.getStateExitedEventDetails.getName, e.getStateExitedEventDetails.getOutput)

      case "ExecutionFailed" => null
      case "ExecutionStarted" => null
      case "ExecutionSucceeded" => null
      case "ExecutionAborted" => null
      case "ExecutionTimedOut" => null

      case "FailStateEntered" => StateStarted(e.getStateEnteredEventDetails.getName)

      case "LambdaFunctionFailed" =>
        EventFailed(
          Option(e.getLambdaFunctionFailedEventDetails.getError),
          Option(e.getLambdaFunctionFailedEventDetails.getCause),
          Failed)
      case "LambdaFunctionScheduleFailed" =>
        EventFailed(
          Option(e.getLambdaFunctionScheduleFailedEventDetails.getError),
          Option(e.getLambdaFunctionScheduleFailedEventDetails.getCause),
          Failed)
      case "LambdaFunctionScheduled" =>
        EventScheduled(
          e.getLambdaFunctionScheduledEventDetails.getInput,
          e.getLambdaFunctionScheduledEventDetails.getResource,
          zeroAsNone(e.getLambdaFunctionScheduledEventDetails.getTimeoutInSeconds),
          None)
      case "LambdaFunctionStartFailed" =>
        EventFailed(
          Option(e.getLambdaFunctionStartFailedEventDetails.getError),
          Option(e.getLambdaFunctionStartFailedEventDetails.getCause),
          Failed)
      case "LambdaFunctionStarted" => EventStarted(None)
      case "LambdaFunctionSucceeded" =>
        EventSucceeded(e.getLambdaFunctionSucceededEventDetails.getOutput)
      case "LambdaFunctionTimedOut" =>
        EventFailed(
          Option(e.getLambdaFunctionTimedOutEventDetails.getError),
          Option(e.getLambdaFunctionTimedOutEventDetails.getCause),
          TimedOut)

      case "SucceedStateEntered" => StateStarted(e.getStateEnteredEventDetails.getName)
      case "SucceedStateExited" =>
        StateSucceeded(e.getStateExitedEventDetails.getName, e.getStateExitedEventDetails.getOutput)

      case "TaskStateAborted" =>
        StateFailed(e.getStateExitedEventDetails.getName, e.getStateExitedEventDetails.getOutput, Aborted)
      case "TaskStateEntered" => StateStarted(e.getStateEnteredEventDetails.getName)
      case "TaskStateExited" =>
        StateSucceeded(e.getStateExitedEventDetails.getName, e.getStateExitedEventDetails.getOutput)

      case "PassStateEntered" => StateStarted(e.getStateEnteredEventDetails.getName)
      case "PassStateExited" =>
        StateSucceeded(e.getStateExitedEventDetails.getName, e.getStateExitedEventDetails.getOutput)

      case "ParallelStateAborted" =>
        StateFailed(e.getStateExitedEventDetails.getName, e.getStateExitedEventDetails.getOutput, Aborted)
      case "ParallelStateEntered" => StateStarted(e.getStateEnteredEventDetails.getName)
      case "ParallelStateExited" =>
        StateSucceeded(e.getStateExitedEventDetails.getName, e.getStateExitedEventDetails.getOutput)
      case "ParallelStateFailed" =>
        StateFailed(e.getStateExitedEventDetails.getName, e.getStateExitedEventDetails.getOutput, Failed)
      case "ParallelStateStarted" => null
      case "ParallelStateSucceeded" => null

      case "WaitStateAborted" =>
        StateFailed(e.getStateExitedEventDetails.getName, e.getStateExitedEventDetails.getOutput, Aborted)
      case "WaitStateEntered" => StateStarted(e.getStateEnteredEventDetails.getName)
      case "WaitStateExited" =>
        StateSucceeded(e.getStateExitedEventDetails.getName, e.getStateExitedEventDetails.getOutput)
    }
  }

  def zeroAsNone(x: Long): Option[Long] = if (x == 0) None else Some(x)
}
