package awscala.ec2

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ ec2 => aws }
import scala.annotation.tailrec

object EC2 {

  def apply(credentials: Credentials)(implicit region: Region): EC2 = new EC2Client(BasicCredentialsProvider(credentials.getAWSAccessKeyId, credentials.getAWSSecretKey)).at(region)
  def apply(credentialsProvider: CredentialsProvider = CredentialsLoader.load())(implicit region: Region = Region.default()): EC2 = new EC2Client(credentialsProvider).at(region)
  def apply(accessKeyId: String, secretAccessKey: String)(implicit region: Region): EC2 = apply(BasicCredentialsProvider(accessKeyId, secretAccessKey)).at(region)

  def at(region: Region): EC2 = apply()(region)
}

/**
 * Amazon EC2 Java client wrapper
 * @see [[http://docs.aws.amazon.com/AWSJavaSDK/latest/javadoc/]]
 */
trait EC2 extends aws.AmazonEC2Async {

  def at(region: Region): EC2 = {
    this.setRegion(region)
    this
  }

  // ------------------------------------------
  // Instances
  // ------------------------------------------

  def instances: Seq[Instance] = {
    describeInstances.getReservations.asScala.flatMap(_.getInstances.asScala.toSeq.map(Instance(_)))
  }

  def instances(instanceId: String*): Seq[Instance] = {
    describeInstances(new aws.model.DescribeInstancesRequest().withInstanceIds(instanceId: _*))
      .getReservations.asScala.flatMap(_.getInstances.asScala).map(Instance(_))
  }

  def instances(instanceIds: Seq[String] = Nil, filters: Seq[aws.model.Filter] = Nil): Seq[Instance] = {
    describeInstances(new aws.model.DescribeInstancesRequest().withInstanceIds(instanceIds.asJava).withFilters(filters.asJava))
      .getReservations.asScala.flatMap(_.getInstances.asScala).map(Instance(_))
  }

  def runAndAwait(
    imageId: String,
    keyPair: KeyPair,
    instanceType: InstanceType = InstanceType.T1_Micro,
    min: Int = 1,
    max: Int = 1): Seq[Instance] = {

    runAndAwait(new RunInstancesRequest(imageId, min, max).withKeyName(keyPair.name).withInstanceType(instanceType))
  }

  @tailrec
  final def awaitInstances(awaiting: Seq[Instance], checkInterval: Long = 5000L): Seq[Instance] = {
    val requested = instances(awaiting.map(_.instanceId))
    if (requested.exists(_.state.getName == aws.model.InstanceStateName.Pending.toString)) {
      Thread.sleep(checkInterval)
      awaitInstances(awaiting, checkInterval)
    } else {
      requested
    }
  }

  def runAndAwait(request: aws.model.RunInstancesRequest): Seq[Instance] = awaitInstances(runInstances(request).getReservation.getInstances.asScala.map(Instance(_)))

  def start(instance: Instance*) = startInstances(new aws.model.StartInstancesRequest()
    .withInstanceIds(instance.map(_.instanceId): _*))

  def stop(instance: Instance*) = stopInstances(new aws.model.StopInstancesRequest()
    .withInstanceIds(instance.map(_.instanceId): _*))

  def terminate(instance: Instance*) = terminateInstances(new aws.model.TerminateInstancesRequest()
    .withInstanceIds(instance.map(_.instanceId): _*))

  def reboot(instance: Instance*) = rebootInstances(new aws.model.RebootInstancesRequest()
    .withInstanceIds(instance.map(_.instanceId): _*))

  // ------------------------------------------
  // Key Pairs
  // ------------------------------------------

  def keyPairs: Seq[KeyPair] = describeKeyPairs.getKeyPairs.asScala.map(KeyPair(_))

  def keyPair(name: String): Option[KeyPair] = {
    describeKeyPairs(new aws.model.DescribeKeyPairsRequest().withKeyNames(name))
      .getKeyPairs.asScala.map(KeyPair(_)).headOption
  }

  def createKeyPair(name: String): KeyPair = KeyPair(createKeyPair(new aws.model.CreateKeyPairRequest(name)).getKeyPair)

  def delete(keyPair: KeyPair): Unit = deleteKeyPair(keyPair.name)
  def deleteKeyPair(name: String): Unit = deleteKeyPair(new aws.model.DeleteKeyPairRequest(name))

  // ------------------------------------------
  // Security Groups
  // ------------------------------------------

  def securityGroups: Seq[SecurityGroup] = describeSecurityGroups.getSecurityGroups.asScala.map(SecurityGroup(_))

  def securityGroup(name: String): Option[SecurityGroup] = {
    describeSecurityGroups(new aws.model.DescribeSecurityGroupsRequest().withGroupNames(name))
      .getSecurityGroups.asScala.map(SecurityGroup(_)).headOption
  }

  def createSecurityGroup(name: String, description: String): Option[SecurityGroup] = {
    createSecurityGroup(new aws.model.CreateSecurityGroupRequest(name, description))
    securityGroup(name)
  }

  def delete(securityGroup: SecurityGroup): Unit = deleteSecurityGroup(securityGroup.groupName)
  def deleteSecurityGroup(name: String): Unit = {
    deleteSecurityGroup(new aws.model.DeleteSecurityGroupRequest().withGroupName(name))
  }

  def tags(filters: Seq[aws.model.Filter] = Nil): Seq[aws.model.TagDescription] = {
    import aws.model.DescribeTagsResult
    object tagsSequencer extends Sequencer[aws.model.TagDescription, DescribeTagsResult, String] {

      val baseRequest = new aws.model.DescribeTagsRequest().withFilters(filters.asJava)
      def getInitial = describeTags(baseRequest)
      def getMarker(r: DescribeTagsResult) = r.getNextToken()
      def getFromMarker(marker: String) = describeTags(baseRequest.withNextToken(marker))
      def getList(r: DescribeTagsResult) = r.getTags()
    }
    tagsSequencer.sequence
  }

  def instanceStatuses(includeAll: Boolean = false, instanceIds: Seq[String] = Nil, filters: Seq[aws.model.Filter] = Nil): Seq[aws.model.InstanceStatus] = {
    import aws.model.DescribeInstanceStatusResult

    object instanceStatusSequencer extends Sequencer[aws.model.InstanceStatus, DescribeInstanceStatusResult, String] {
      val baseRequest = new aws.model.DescribeInstanceStatusRequest().withIncludeAllInstances(includeAll).withInstanceIds(instanceIds.asJava).withFilters(filters.asJava)
      def getInitial = describeInstanceStatus(baseRequest)
      def getMarker(r: DescribeInstanceStatusResult) = r.getNextToken()
      def getFromMarker(marker: String) = describeInstanceStatus(baseRequest.withNextToken(marker))
      def getList(r: DescribeInstanceStatusResult) = r.getInstanceStatuses()
    }
    instanceStatusSequencer.sequence
  }

  def reservedInstanceOfferings(availabilityZone: Option[String] = None, filters: Seq[aws.model.Filter] = Nil): Seq[aws.model.ReservedInstancesOffering] = {
    import aws.model.DescribeReservedInstancesOfferingsResult

    object reservedSequencer extends Sequencer[aws.model.ReservedInstancesOffering, DescribeReservedInstancesOfferingsResult, String] {
      val baseRequest = new aws.model.DescribeReservedInstancesOfferingsRequest().withFilters(filters.asJava)
      val base = if (availabilityZone == None) baseRequest else baseRequest.withAvailabilityZone(availabilityZone.get)
      def getInitial = describeReservedInstancesOfferings(base)
      def getMarker(r: DescribeReservedInstancesOfferingsResult) = r.getNextToken()
      def getFromMarker(marker: String) = describeReservedInstancesOfferings(base.withNextToken(marker))
      def getList(r: DescribeReservedInstancesOfferingsResult) = r.getReservedInstancesOfferings()
    }
    reservedSequencer.sequence
  }
}

/**
 * Default Implementation
 *
 * @param credentials credentials
 */
class EC2Client(credentialsProvider: CredentialsProvider = CredentialsLoader.load())
  extends aws.AmazonEC2AsyncClient(credentialsProvider)
  with EC2
