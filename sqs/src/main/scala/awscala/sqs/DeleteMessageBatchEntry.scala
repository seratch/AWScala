package awscala.sqs

case class DeleteMessageBatchEntry(id: String, receiptHandle: String)
  extends com.amazonaws.services.sqs.model.DeleteMessageBatchRequestEntry(id, receiptHandle)

