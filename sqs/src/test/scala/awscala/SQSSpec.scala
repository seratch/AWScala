package awscala

import awscala._, sqs._

import org.slf4j._
import org.scalatest._

class SQSSpec extends FlatSpec with Matchers {

  behavior of "SQS"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "provide cool APIs" in {
    implicit val sqs = SQS.at(Region.Tokyo)

    // queues
    val queues = sqs.queues
    log.info(s"Queues : ${queues}")

    // create new queue
    val newQueueName = s"sample-queue-${System.currentTimeMillis}"
    val queue = sqs.createQueueAndReturnQueueName(newQueueName)
    val url = sqs.queueUrl(newQueueName)
    log.info(s"Created queue: ${queue}, url: ${url}")

    // get queue attributes before inserting any message
    val attribute = sqs.queueAttributes(queue, "ApproximateNumberOfMessages")
    log.info(s"Attribute for queue before inserting any message")
    attribute.keys.foreach { i =>
      log.info(s"Attribute Name = ${i}")
      log.info(s"Value = ${attribute}(${i})")
    }

    // send messages
    val sent = queue.add("some message!")
    log.info(s"Sent : ${sent}")
    val sendMessages = queue.add("first", "second", "third")
    log.info(s"Batch Sent : ${sendMessages}")

    // get queue attributes after inserting any message
    val attribute2 = sqs.queueAttributes(queue, "ApproximateNumberOfMessages")
    log.info(s"Attribute for queue after inserting any message")
    attribute2.keys.foreach { i =>
      log.info(s"Attribute Name = ${i}")
      log.info(s"Value = ${attribute2}(${i})")
    }

    // receive messages
    val receivedMessages = queue.messages // or sqs.receiveMessage(queue)
    log.info(s"Received : ${receivedMessages}")

    // delete messages
    queue.removeAll(receivedMessages)

    // working with specified queue
    sqs.withQueue(queue) { s =>
      s.sendMessage("some message!")
      s.sendMessages("first", "second", "third")
      s.receiveMessage.foreach(msg => s.deleteMessage(msg))
    }

    // delete a queue
    queue.destroy() // or sqs.deleteQueue(queue)
    log.info(s"Deleted queue: ${queue}")

  }

}
