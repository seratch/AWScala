package awscala.sqs

import scala.collection.JavaConverters._

object Message {

  def apply(queue: Queue, msg: com.amazonaws.services.sqs.model.Message) = new Message(
    queue = queue,
    id = msg.getMessageId,
    body = msg.getBody,
    receiptHandle = msg.getReceiptHandle,
    attributes = msg.getAttributes.asScala.toMap
  )
}

case class Message(queue: Queue, id: String, body: String, receiptHandle: String, attributes: Map[String, String])
    extends com.amazonaws.services.sqs.model.Message {

  setMessageId(id)
  setBody(body)
  setReceiptHandle(receiptHandle)
  setAttributes(attributes.asJava)

  def destroy()(implicit sqs: SQS): Unit = sqs.deleteMessage(this)

}

