package awscala.stepfunctions

import java.util
import java.util.concurrent.TimeUnit

import awscala._
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.services.stepfunctions.model._
import com.amazonaws.services.{ stepfunctions => aws }

import scala.collection.JavaConverters._
import scala.concurrent.{ blocking, ExecutionContext, Future }
import scala.util.{ Failure, Success, Try }

object StepFunctions {
  private val DEFAULT_SOCKET_TIMEOUT = TimeUnit.SECONDS.toMillis(70).toInt

  def apply(credentials: Credentials)(implicit region: Region): StepFunctions =
    apply(BasicCredentialsProvider(credentials.getAWSAccessKeyId, credentials.getAWSSecretKey))(region)

  def apply(accessKeyId: String, secretAccessKey: String)(implicit region: Region): StepFunctions =
    apply(BasicCredentialsProvider(accessKeyId, secretAccessKey))(region)

  def apply(credentialsProvider: AWSCredentialsProvider = CredentialsLoader.load())(
    implicit
    region: Region = Region.default()): StepFunctions =
    new StepFunctionsClient(new ClientConfiguration().withSocketTimeout(DEFAULT_SOCKET_TIMEOUT), credentialsProvider)
      .at(region)

  def apply(clientConfiguration: ClientConfiguration, credentials: Credentials)(
    implicit
    region: Region): StepFunctions =
    apply(clientConfiguration, BasicCredentialsProvider(credentials.getAWSAccessKeyId, credentials.getAWSSecretKey))(
      region)

  def apply(clientConfiguration: ClientConfiguration, accessKeyId: String, secretAccessKey: String)(
    implicit
    region: Region): StepFunctions =
    apply(clientConfiguration, BasicCredentialsProvider(accessKeyId, secretAccessKey))(region)

  def apply(clientConfiguration: ClientConfiguration, credentialsProvider: AWSCredentialsProvider)(
    implicit
    region: Region): StepFunctions =
    new StepFunctionsClient(
      clientConfiguration.withSocketTimeout(DEFAULT_SOCKET_TIMEOUT),
      credentialsProvider).at(region)

  def at(region: Region): StepFunctions = apply()(region)
}

trait StepFunctions extends aws.AWSStepFunctions {
  def at(region: Region): StepFunctions = {
    this.setRegion(region)
    this
  }

  def createStateMachine(name: String, definition: String, roleArn: String): StateMachine = StateMachine {
    createStateMachine(
      new CreateStateMachineRequest()
        .withName(name)
        .withDefinition(definition)
        .withRoleArn(roleArn)).getStateMachineArn
  }

  def stateMachine(name: String): Option[StateMachine] = stateMachines.find(_.name == name)

  def stateMachines: Seq[StateMachine] = {
    object StateMachineSequencer extends Sequencer[StateMachineListItem, ListStateMachinesResult, String] {
      val base = new ListStateMachinesRequest()
      def getInitial: ListStateMachinesResult = listStateMachines(base)

      def getMarker(r: ListStateMachinesResult): String = r.getNextToken

      def getFromMarker(marker: String): ListStateMachinesResult = listStateMachines(base.withNextToken(marker))

      def getList(r: ListStateMachinesResult): util.List[StateMachineListItem] = r.getStateMachines
    }
    StateMachineSequencer.sequence.map(sm => StateMachine(sm.getStateMachineArn))
  }

  def createActivity(name: String): Activity = Activity {
    createActivity(new CreateActivityRequest().withName(name)).getActivityArn
  }

  def activity(name: String): Option[Activity] = activities.find(_.name == name)

  def activities: Seq[Activity] = {
    object ActivitiesSequencer extends Sequencer[ActivityListItem, ListActivitiesResult, String] {
      private val base = new ListActivitiesRequest()
      def getInitial: ListActivitiesResult = listActivities(base)

      def getMarker(r: ListActivitiesResult): String = r.getNextToken

      def getFromMarker(marker: String): ListActivitiesResult = listActivities(base.withNextToken(marker))

      def getList(r: ListActivitiesResult): util.List[ActivityListItem] = r.getActivities
    }
    ActivitiesSequencer.sequence.map(a => Activity(a.getActivityArn))
  }

  def runActivity(name: String, workerName: String = null)(fn: String => String)(
    implicit
    ec: ExecutionContext): Future[Option[String]] =
    Future {
      activity(name).map(_.arn).map { arn =>
        blocking {
          getActivityTask(new GetActivityTaskRequest().withActivityArn(arn).withWorkerName(workerName))
        }
      }
    }.map { s =>
      s.flatMap(ac => Option(ac.getTaskToken).map((_, ac.getInput)))
        .map {
          case (token, input) =>
            val result = Try(fn(input))
            result match {
              case Failure(err) =>
                sendTaskFailure(
                  new SendTaskFailureRequest()
                    .withTaskToken(token)
                    .withCause(Option(err.getCause).map(_.getMessage).orNull)
                    .withError(err.getMessage))
                throw err
              case Success(output) =>
                sendTaskSuccess(new SendTaskSuccessRequest().withTaskToken(token).withOutput(output))
                output
            }
        }
    }
}

class StepFunctionsClient(
  clientConfiguration: ClientConfiguration,
  credentialsProvider: AWSCredentialsProvider = CredentialsLoader.load())
  extends aws.AWSStepFunctionsClient(credentialsProvider, clientConfiguration)
  with StepFunctions
