package awscala.ec2

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ ec2 => aws }

object EC2 {
  def apply(credentials: Credentials = Credentials.defaultEnv): EC2 = new EC2Client(credentials)

  def apply(accessKeyId: String, secretAccessKey: String): EC2 = apply(Credentials(accessKeyId, secretAccessKey))

  def at(region: Region): EC2 = apply().at(region)
}

trait EC2 extends aws.AmazonEC2 {
  lazy val CHECK_INTERVAL = 5000L

  def at(region: Region): EC2 = {
    this.setRegion(region)
    this
  }

  def instances: Seq[Instance] = describeInstances().getReservations().asScala.flatMap(_.getInstances.asScala.toSeq.map(Instance(_)))

  def instance(instanceId: String*): Option[Instance] = {
    describeInstances(new aws.model.DescribeInstancesRequest().withInstanceIds(instanceId: _*)).getReservations.asScala.flatMap(_.getInstances.asScala).headOption.map(Instance(_))
  }

  def run(request: aws.model.RunInstancesRequest): Seq[Instance] = {
    var requestedInstances: Seq[Instance] = runInstances(request).getReservation.getInstances.asScala.map(Instance(_))
    val ids = requestedInstances.map(_.instanceId)

    def checkStatus(checkIds: Seq[String]): Seq[Instance] = instances.filter(i => checkIds.contains(i.instanceId))

    val pendingState = new aws.model.InstanceState().withName(aws.model.InstanceStateName.Pending)
    while (requestedInstances.exists(_.state.getName == pendingState.getName)) {
      Thread.sleep(CHECK_INTERVAL)
      requestedInstances = checkStatus(ids)
    }
    requestedInstances
  }
}

/**
 * Default Implementation
 *
 * @param credentials credentials
 */
class EC2Client(credentials: Credentials = Credentials.defaultEnv)
    extends aws.AmazonEC2Client(credentials)
    with EC2 {
}

