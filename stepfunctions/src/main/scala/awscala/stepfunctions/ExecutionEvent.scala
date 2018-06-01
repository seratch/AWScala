package awscala.stepfunctions

import awscala.DateTime
import awscala.stepfunctions.ExecutionEventDetails.ExecutionEventDetails

case class ExecutionEvent(
  id: Long,
  previousId: Long,
  timestamp: DateTime,
  details: ExecutionEventDetails)
