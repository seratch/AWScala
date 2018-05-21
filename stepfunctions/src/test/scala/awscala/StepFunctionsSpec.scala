package awscala

import java.util.concurrent.TimeUnit

import awscala.iam.IAM
import awscala.stepfunctions.ExecutionStatus.{ Failed, NotStarted, Succeeded }
import awscala.stepfunctions.StepFunctions
import com.amazonaws.auth.policy.Principal
import org.scalatest._
import org.slf4j.LoggerFactory

import scala.concurrent.duration.Duration
import scala.concurrent.{ Await, Future }
import scala.util.Try

class StepFunctionsSpec extends FlatSpec with Matchers {

  behavior of "StepFunctions"

  it should "provide cool APIs" in {
    implicit val steps = StepFunctions.at(Region.US_WEST_2)
    implicit val iam = IAM()
    val role = iam
      .createRole(
        "awscala-test-stepfunctions",
        "/service-role/",
        Policy(
          Seq(
            Statement(
              Effect.Allow,
              Seq(Action("sts:AssumeRole")),
              Seq.empty,
              principals = Seq(new Principal("Service", "states.us-west-2.amazonaws.com"))))))
    val failActivity = steps.createActivity("fail")
    val succeedActivity = steps.createActivity("succeed")
    val parallelActivity = steps.createActivity("parallel")
    val machineDefinition =
      s"""
         |{
         |  "StartAt": "Test Type",
         |  "States": {
         |    "Test Type": {
         |      "Type": "Choice",
         |      "Choices": [
         |        {
         |          "Variable": "$$.type",
         |          "StringEquals": "FAIL",
         |          "Next": "Fail Step"
         |        },
         |        {
         |          "Variable": "$$.type",
         |          "StringEquals": "PARALLEL",
         |          "Next": "Parallel Step"
         |        }
         |      ],
         |      "Default": "Succeed Step"
         |    },
         |    "Fail Step": {
         |      "Type": "Task",
         |      "TimeoutSeconds": 5,
         |      "Resource": "${failActivity.arn}",
         |      "End": true
         |    },
         |    "Succeed Step": {
         |      "Type": "Task",
         |      "TimeoutSeconds": 5,
         |      "Resource": "${succeedActivity.arn}",
         |      "Next": "Succeed Step Again"
         |    },
         |    "Succeed Step Again": {
         |      "Type": "Task",
         |      "TimeoutSeconds": 5,
         |      "Resource": "${succeedActivity.arn}",
         |      "End": true
         |    },
         |    "Parallel Step": {
         |      "Type": "Parallel",
         |      "Branches": [
         |        {
         |          "StartAt": "ParPass",
         |          "States": {
         |            "ParPass": {
         |              "Type": "Pass",
         |              "End": true
         |            }
         |          }
         |        },
         |        {
         |          "StartAt": "Inner Parallel",
         |          "States": {
         |            "Inner Parallel": {
         |              "Type": "Task",
         |              "Resource": "${parallelActivity.arn}",
         |              "End": true
         |            }
         |          }
         |        }
         |      ],
         |      "End": true
         |    }
         |  }
         |}
      """.stripMargin
    val machine = steps.createStateMachine("test", machineDefinition, role.arn)

    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    try {
      val input = """{"type":"SUCCEED"}"""
      val exec = machine.startExecution(input)
      Await.ready(
        Future.sequence {
          Seq(steps.runActivity(succeedActivity.name)(identity), steps.runActivity(succeedActivity.name)(identity))
        },
        Duration(30, TimeUnit.SECONDS))
      assert(exec.getStepStatus("Succeed Step") === Succeeded)
      assert(exec.getStepStatus("Succeed Step Again") === Succeeded)
      assert(exec.getStepStatus("Fail Step") === NotStarted)
      val details = exec.details
      assert(details.status === Succeeded)
      assert(details.output === Some(details.input))
      assert(details.input === input)

      val exec2 = machine.startExecution("""{"type":"FAIL"}""")
      intercept[IllegalArgumentException] {
        Await.result(
          steps.runActivity(failActivity.name)(_ => throw new IllegalArgumentException()),
          Duration(30, TimeUnit.SECONDS))
      }
      assert(exec2.getStepStatus("Fail Step") === Failed)
      assert(exec2.getStepStatus("Succeed Step") === NotStarted)

      val exec3 = machine.startExecution("""{"type":"PARALLEL"}""")
      Await.result(steps.runActivity(parallelActivity.name)(identity), Duration(30, TimeUnit.SECONDS))
      assert(exec3.getStepStatus("Inner Parallel") === Succeeded)
      assert(exec3.getStepStatus("Fail Step") === NotStarted)
      assert(exec3.getStepStatus("Succeed Step") === NotStarted)

      assert(steps.stateMachines.contains(machine))
      assert(steps.stateMachine(machine.name) === Some(machine))
      assert(machine.executions() === Seq(exec3, exec2, exec))
      assert(machine.execution(exec.name) === Some(exec))
      assert(machine.definition() === machineDefinition)
    } finally {
      machine.delete()
      role.destroy()
      failActivity.delete()
      succeedActivity.delete()
      parallelActivity.delete()
    }
  }
}
