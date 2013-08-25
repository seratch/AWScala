package awscala

import awscala._, sqs._

import org.slf4j._
import org.scalatest._
import org.scalatest.matchers._

class SQSSpec extends FlatSpec with ShouldMatchers {

  behavior of "SQS"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "provide cool APIs" in {
    implicit val sqs = SQS.at(Region.Tokyo)

    // queues
    val queues = sqs.queues
    log.info(s"Queues : ${queues}")

    // create new queue
    val newQueueName = s"sample-queue-${System.currentTimeMillis}"
    val queue = sqs.createQueue(newQueueName)
    val url = sqs.queueUrl(newQueueName)
    log.info(s"Created queue: ${queue}, url: ${url}")

    // send messages
    val sent = sqs.sendMessage(queue, "some message!")
    log.info(s"Sent : ${sent}")
    val sents = sqs.sendMessages(queue, Seq("first", "second", "third"))
    log.info(s"Batch Sent : ${sents}")

    // receive messages
    val receivedMessages = sqs.receiveMessage(queue)
    log.info(s"Received : ${receivedMessages}")

    // delete messages
    sqs.deleteMessages(receivedMessages)
    receivedMessages.foreach(msg => sqs.deleteMessage(msg))
    receivedMessages.foreach(_.destroy()) // working with implicit SQS instance

    // working with specified queue
    sqs.withQueue(queue) { s =>
      s.sendMessage("some message!")
      s.sendMessages(Seq("first", "second", "third"))
      s.receiveMessage.foreach(msg => s.deleteMessage(msg))
    }

    // delete a queue
    queue.destroy() // or sqs.deleteQueue(queue)
    log.info(s"Deleted queue: ${queue}")
  }

}
