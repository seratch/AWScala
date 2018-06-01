package awscala.stepfunctions

import awscala.stepfunctions.ArnFormat.ResourceArn
import com.amazonaws.services.stepfunctions.model.DeleteActivityRequest

case class Activity(arn: String) {
  val name: String = ArnFormat.parseArn(arn, ResourceArn)

  def delete()(implicit steps: StepFunctions): Unit =
    steps.deleteActivity(new DeleteActivityRequest().withActivityArn(arn))
}
