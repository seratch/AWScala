package awscala.sqs

case class MessageBatchEntry(id: String, messageBody: String)
  extends com.amazonaws.services.sqs.model.SendMessageBatchRequestEntry(id, messageBody)

