package awscala.stepfunctions

import java.util

import awscala.stepfunctions.ArnFormat.TypedResourceArn
import awscala.stepfunctions.ExecutionEventDetails.{ EventFailed, StateFailed, StateStarted, StateSucceeded }
import awscala.stepfunctions.ExecutionStatus.{ ExecutionStatus, Failed, NotStarted, Running, Succeeded }
import awscala.{ DateTime, Sequencer }
import com.amazonaws.services.stepfunctions.model.{ DescribeExecutionRequest, GetExecutionHistoryRequest, GetExecutionHistoryResult, HistoryEvent }

import scala.annotation.tailrec

case class Execution(arn: String, startTime: DateTime) {
  def name: String = ArnFormat.parseArn(arn, TypedResourceArn)

  def details()(implicit steps: StepFunctions): ExecutionDetails = {
    val details = steps.describeExecution(new DescribeExecutionRequest().withExecutionArn(arn))
    ExecutionDetails(
      arn,
      startTime,
      Option(details.getStopDate).map(new DateTime(_)),
      ExecutionStatus.fromString(details.getStatus),
      details.getInput,
      Option(details.getOutput))
  }

  def getStepStatus(name: String)(implicit steps: StepFunctions): ExecutionStatus = {
    val hist = history()
    def getById(id: Long): Option[ExecutionEvent] = hist.find(_.id == id)
    @tailrec
    def startedByEvent(id: Long): Boolean =
      getById(id) match {
        case Some(ExecutionEvent(_, _, _, StateStarted(`name`))) => true
        case Some(ExecutionEvent(_, _, _, StateStarted(_))) => false
        case None => false
        case Some(ExecutionEvent(_, prev, _, _)) => startedByEvent(prev)
      }

    val started = hist.exists {
      case ExecutionEvent(_, _, _, StateStarted(`name`)) => true
      case _ => false
    }
    val succeeded = hist.exists {
      case ExecutionEvent(_, _, _, StateSucceeded(`name`, _)) => true
      case _ => false
    }
    val failedId = hist.collect {
      case ExecutionEvent(id, _, _, StateFailed(`name`, _, _)) => Some(id)
      case ExecutionEvent(id, prev, _, EventFailed(_, _, _)) if startedByEvent(prev) => id
    }.lastOption

    if (!started) {
      NotStarted
    } else if (succeeded) {
      Succeeded
    } else {
      failedId
        .map { id =>
          val nextEvent = hist.find {
            case ExecutionEvent(_, `id`, _, _) => true
            case _ => false
          }
          nextEvent match {
            case Some(ExecutionEvent(_, _, _, StateStarted(_))) => Running // We retried the failure
            case _ => Failed
          }
        }
        .getOrElse(Running) // This could still be retried
    }
  }

  def history()(implicit steps: StepFunctions): Seq[ExecutionEvent] = {
    object HistorySequencer extends Sequencer[HistoryEvent, GetExecutionHistoryResult, String] {
      private val base = new GetExecutionHistoryRequest().withExecutionArn(arn)
      def getInitial: GetExecutionHistoryResult = steps.getExecutionHistory(base)

      def getMarker(r: GetExecutionHistoryResult): String = r.getNextToken

      def getFromMarker(marker: String): GetExecutionHistoryResult =
        steps.getExecutionHistory(base.withNextToken(marker))

      def getList(r: GetExecutionHistoryResult): util.List[HistoryEvent] = r.getEvents
    }
    HistorySequencer.sequence.flatMap { rawEvent =>
      ExecutionEventDetails.fromEvent(rawEvent).map { event =>
        ExecutionEvent(rawEvent.getId, rawEvent.getPreviousEventId, new DateTime(rawEvent.getTimestamp), event)
      }
    }
  }

  def status()(implicit steps: StepFunctions): ExecutionStatus = details().status
  def endTime()(implicit steps: StepFunctions): Option[DateTime] = details().endTime
  def input()(implicit steps: StepFunctions): String = details().input
  def output()(implicit steps: StepFunctions): Option[String] = details().output
}
