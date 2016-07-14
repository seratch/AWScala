package awscala.sqs

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ sqs => aws }
import com.amazonaws.auth.AWSSessionCredentials

object SQS {

  def apply(credentials: Credentials)(implicit region: Region): SQS = new SQSClient(BasicCredentialsProvider(credentials.getAWSAccessKeyId, credentials.getAWSSecretKey)).at(region)
  def apply(credentialsProvider: CredentialsProvider = CredentialsLoader.load())(implicit region: Region = Region.default()): SQS = new SQSClient(credentialsProvider).at(region)
  def apply(accessKeyId: String, secretAccessKey: String)(implicit region: Region): SQS = apply(BasicCredentialsProvider(accessKeyId, secretAccessKey)).at(region)

  def at(region: Region): SQS = apply()(region)
}

/**
 * Amazon Simple Queue Service Java client wrapper
 * @see [[http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/]]
 */
trait SQS extends aws.AmazonSQS {

  def at(region: Region): SQS = {
    this.setRegion(region)
    this
  }

  // ------------------------------------------
  // Queues
  // ------------------------------------------

  // createQueue is added since SDK 1.7.x
  def createQueueAndReturnQueueName(name: String): Queue = {
    val result = createQueue(new aws.model.CreateQueueRequest(name))
    Queue(result.getQueueUrl)
  }
  def delete(queue: Queue): Unit = deleteQueue(queue)
  def deleteQueue(queue: Queue): Unit = deleteQueue(new aws.model.DeleteQueueRequest(queue.url))

  def queues: Seq[Queue] = listQueues().getQueueUrls.asScala.map(url => Queue(url)).toSeq

  def queue(name: String): Option[Queue] = queues.find(_.url.split("/").last == name)

  def queueUrl(name: String): Option[String] = try {
    Some(getQueueUrl(new aws.model.GetQueueUrlRequest(name)).getQueueUrl)
  } catch {
    case e: aws.model.QueueDoesNotExistException => None
  }

  def withQueue[A](queue: Queue)(op: (SQSClientWithQueue) => A): A = op(new SQSClientWithQueue(this, queue))

  def queueAttributes(queue: Queue, attributeName: String): Map[String, String] = {
    val result = getQueueAttributes(new aws.model.GetQueueAttributesRequest(queue.url, List(attributeName).asJava))
    result.getAttributes.asScala.toMap
  }

  // ------------------------------------------
  // Messages
  // ------------------------------------------

  def send(queue: Queue, messageBody: String): aws.model.SendMessageResult = sendMessage(queue, messageBody)
  def sendMessage(queue: Queue, messageBody: String): aws.model.SendMessageResult = {
    sendMessage(new aws.model.SendMessageRequest(queue.url, messageBody))
  }
  def sendMessages(queue: Queue, messageBodies: Seq[String]): aws.model.SendMessageBatchResult = {
    val batchId = Thread.currentThread.getId + "-" + System.nanoTime
    sendMessageBatch(queue, messageBodies.zipWithIndex.map { case (body, idx) => new MessageBatchEntry(s"${batchId}-${idx}", body) })
  }
  def sendMessageBatch(queue: Queue, messages: Seq[MessageBatchEntry]): aws.model.SendMessageBatchResult = {
    sendMessageBatch(new aws.model.SendMessageBatchRequest(
      queue.url,
      messages.map(_.asInstanceOf[aws.model.SendMessageBatchRequestEntry]).asJava
    ))
  }

  def receive(queue: Queue): Seq[Message] = receiveMessage(queue)
  def receiveMessage(queue: Queue): Seq[Message] = receiveMessage(queue, 1)
  def receiveMessage(queue: Queue, count: Int = 10, wait: Int = 0, requestCredentials: Option[AWSSessionCredentials] = None): Seq[Message] = {
    val req = new aws.model.ReceiveMessageRequest(queue.url).withMaxNumberOfMessages(count).withWaitTimeSeconds(wait)
    requestCredentials.foreach(c => req.setRequestCredentials(c))
    receiveMessage(req).getMessages.asScala.map(msg => Message(queue, msg)).toSeq
  }

  def delete(message: Message) = deleteMessage(message)
  def deleteMessage(message: Message, requestCredentials: Option[AWSSessionCredentials] = None): Unit = {
    val request = new aws.model.DeleteMessageRequest(message.queue.url, message.receiptHandle)
    requestCredentials.foreach(c => request.setRequestCredentials(c))
    deleteMessage(request)
  }
  def deleteMessages(messages: Seq[Message], requestCredentials: Option[AWSSessionCredentials] = None): Unit = {
    val batchId = Thread.currentThread.getId + "-" + System.nanoTime
    deleteMessageBatch(
      messages.head.queue,
      messages.zipWithIndex.map { case (msg, idx) => new DeleteMessageBatchEntry(s"${batchId}-${idx}", msg.receiptHandle) },
      requestCredentials
    )
  }
  def deleteMessageBatch(queue: Queue, messages: Seq[DeleteMessageBatchEntry], requestCredentials: Option[AWSSessionCredentials] = None): Unit = {
    val request = new aws.model.DeleteMessageBatchRequest(
      queue.url,
      messages.map(_.asInstanceOf[aws.model.DeleteMessageBatchRequestEntry]).asJava
    )
    requestCredentials.foreach(c => request.setRequestCredentials(c))
    deleteMessageBatch(request)
  }

}

/**
 * SQSClient with specified queue.
 *
 * {{{
 *   val sqs = SQS.at(Region.Tokyo)
 *   sqs.withQueue(sqs.queue("queue-name").get) { s =>
 *     s.sendMessage("only body!")
 *   }
 * }}}
 *
 * @param sqs sqs
 * @param queue queue
 */
class SQSClientWithQueue(sqs: SQS, queue: Queue) {

  def sendMessage(messageBody: String) = sqs.sendMessage(queue, messageBody)
  def sendMessages(messages: String*) = sqs.sendMessages(queue, messages)
  def sendMessageBatch(messages: MessageBatchEntry*) = sqs.sendMessageBatch(queue, messages)

  def receive() = receiveMessage()
  def receiveMessage() = sqs.receiveMessage(queue)

  def delete(message: Message) = sqs.delete(message)
  def deleteMessage(message: Message) = sqs.deleteMessage(message)
  def deleteMessages(messages: Message*) = sqs.deleteMessages(messages)
  def deleteMessageBatch(messages: DeleteMessageBatchEntry*) = sqs.deleteMessageBatch(queue, messages)
  def queueAttributes(attributeName: String) = sqs.queueAttributes(queue, attributeName)
}

/**
 * Default Implementation
 *
 * @param credentialsProvider credentialsProvider
 */
class SQSClient(credentialsProvider: CredentialsProvider = CredentialsLoader.load())
  extends aws.AmazonSQSClient(credentialsProvider)
  with SQS

