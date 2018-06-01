package awscala.stepfunctions

import awscala.DateTime
import awscala.stepfunctions.ExecutionStatus.ExecutionStatus

case class ExecutionDetails(
  arn: String,
  startTime: DateTime,
  endTime: Option[DateTime],
  status: ExecutionStatus,
  input: String,
  output: Option[String])
