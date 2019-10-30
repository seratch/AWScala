package awscala.ec2

import awscala._
import scala.jdk.CollectionConverters._
import com.amazonaws.services.{ ec2 => aws }
import java.util.Date

object Instance {

  def apply(underlying: aws.model.Instance) = new Instance(underlying)
}

class Instance(val underlying: aws.model.Instance) {

  def start()(implicit ec2: EC2) = ec2.start(this)
  def stop()(implicit ec2: EC2) = ec2.stop(this)
  def terminate()(implicit ec2: EC2) = ec2.terminate(this)
  def reboot()(implicit ec2: EC2) = ec2.reboot(this)

  def withKeyPair[T](keyPairFile: File, user: String = "ec2-user", connectionTimeout: Int = 30000)(f: InstanceWithKeyPair => T): T = {
    f(InstanceWithKeyPair(underlying, keyPairFile, user, connectionTimeout))
  }

  def createImage(imageName: String)(implicit ec2: EC2) = {
    ec2.createImage(new aws.model.CreateImageRequest(instanceId, imageName))
  }

  def getName: Option[String] = tags.get("Name")
  def name: String = tags("Name")

  def instanceId: String = underlying.getInstanceId

  def instanceType: String = underlying.getInstanceType

  def imageId: String = underlying.getImageId

  def keyName: String = underlying.getKeyName

  def publicDnsName: String = underlying.getPublicDnsName

  def publicIpAddress: String = underlying.getPublicIpAddress

  def privateDnsName: String = underlying.getPrivateDnsName

  def privateIpAddress: String = underlying.getPrivateIpAddress

  def tags: Map[String, String] = underlying.getTags.asScala.map(t => t.getKey -> t.getValue).toMap

  def amiLaunchIndex: Int = underlying.getAmiLaunchIndex

  def architecture: String = underlying.getArchitecture

  def blockDeviceMappings: Seq[aws.model.InstanceBlockDeviceMapping] = underlying.getBlockDeviceMappings.asScala.toSeq

  def clientToken: String = underlying.getClientToken

  def ebsOptimized: Boolean = underlying.getEbsOptimized

  def hypervisor: Option[String] = Option(underlying.getHypervisor)

  def iamInstanceProfile: Option[aws.model.IamInstanceProfile] = Option(underlying.getIamInstanceProfile)

  def getInstanceLifecycle: Option[String] = Option(instanceLifecycle)

  def instanceLifecycle: String = underlying.getInstanceLifecycle

  def kernelId: String = underlying.getKernelId

  def launchTime: Date = underlying.getLaunchTime

  def monitoring: aws.model.Monitoring = underlying.getMonitoring

  def networkInterfaces: Seq[aws.model.InstanceNetworkInterface] = underlying.getNetworkInterfaces.asScala.toSeq

  def placement: aws.model.Placement = underlying.getPlacement

  def platform: Option[String] = Option(underlying.getPlatform)

  def productCodes: Seq[aws.model.ProductCode] = underlying.getProductCodes.asScala.toSeq

  def getRamdiskId: Option[String] = Option(ramdiskId)

  def ramdiskId: String = underlying.getRamdiskId

  def rootDeviceName: String = underlying.getRootDeviceName

  def rootDeviceType: String = underlying.getRootDeviceType

  def securityGroups: Seq[aws.model.GroupIdentifier] = underlying.getSecurityGroups.asScala.toSeq

  def spotInstanceRequestId: Option[String] = Option(underlying.getSpotInstanceRequestId)

  def state: aws.model.InstanceState = underlying.getState

  def stateReason: Option[aws.model.StateReason] = Option(underlying.getStateReason)

  //this sometimes returns empty string "" but seems not to return null.
  def stateTransitionReason: String = underlying.getStateTransitionReason

  def subnetId: Option[String] = Option(underlying.getSubnetId)

  def sourceDestCheck: Boolean = underlying.getSourceDestCheck

  def virtualizationType: Option[String] = Option(underlying.getVirtualizationType)

  def vpcId: Option[String] = Option(underlying.getVpcId)

  override def toString: String = s"Instance(${underlying.toString})"

}
