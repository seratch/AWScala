package awscala.stepfunctions

import awscala.DateTime
import awscala.stepfunctions.ArnFormat.ResourceArn
import com.amazonaws.services.stepfunctions.model._

import scala.collection.JavaConverters._

case class StateMachine(arn: String) {
  def name: String = ArnFormat.parseArn(arn, ResourceArn)

  def startExecution(input: String, name: String = null)(implicit steps: StepFunctions): Execution = {
    val exec =
      steps.startExecution {
        new StartExecutionRequest()
          .withStateMachineArn(arn)
          .withInput(input)
          .withName(name)
      }
    Execution(exec.getExecutionArn, new DateTime(exec.getStartDate))
  }

  def execution(name: String)(implicit steps: StepFunctions): Option[Execution] = executions().find(_.name == name)

  def executions()(implicit steps: StepFunctions): Seq[Execution] =
    steps
      .listExecutions(new ListExecutionsRequest().withStateMachineArn(arn))
      .getExecutions
      .asScala
      .map(e => Execution(e.getExecutionArn, new DateTime(e.getStartDate)))

  def definition()(implicit steps: StepFunctions): String =
    steps.describeStateMachine(new DescribeStateMachineRequest().withStateMachineArn(arn)).getDefinition

  def update(definition: Option[String] = None, roleArn: Option[String] = None)(implicit steps: StepFunctions): Unit =
    if (definition.isDefined || roleArn.isDefined) {
      steps.updateStateMachine(
        new UpdateStateMachineRequest()
          .withStateMachineArn(arn)
          .withDefinition(definition.orNull)
          .withRoleArn(roleArn.orNull))
    }

  def delete()(implicit steps: StepFunctions): Unit =
    steps.deleteStateMachine(new DeleteStateMachineRequest().withStateMachineArn(arn))
}
