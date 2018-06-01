package awscala.stepfunctions

object ArnFormat {
  sealed trait ArnFormat
  case object TypedResourceArn extends ArnFormat
  case object ResourceArn extends ArnFormat

  def parseArn(arn: String, format: ArnFormat): String = {
    val limit = format match {
      case TypedResourceArn => 8
      case ResourceArn => 7
    }
    arn.split(":", limit).last
  }
}
