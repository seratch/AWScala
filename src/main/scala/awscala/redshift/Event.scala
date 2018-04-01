package awscala.redshift

import awscala._
import com.amazonaws.services.{ redshift => aws }

object Event {

  def apply(e: aws.model.Event): Event = new Event(
    sourceIdentifier = e.getSourceIdentifier,
    sourceType = aws.model.SourceType.fromValue(e.getSourceType),
    message = e.getMessage,
    createdAt = new DateTime(e.getDate))
}

case class Event(
  sourceIdentifier: String,
  sourceType: aws.model.SourceType,
  message: String,
  createdAt: DateTime) extends aws.model.Event {

  setDate(createdAt.toDate)
  setMessage(message)
  setSourceIdentifier(sourceIdentifier)
  setSourceType(sourceType)
}

