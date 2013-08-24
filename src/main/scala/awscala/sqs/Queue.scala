package awscala.sqs

case class Queue(url: String) {

  def destroy()(implicit sqs: SQS) = sqs.deleteQueue(this)

}

