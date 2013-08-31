package awscala.ec2

import scala.collection.JavaConverters._
import com.amazonaws.services.{ ec2 => aws }
import java.util.Date
import java.io.File
import com.decodified.scalassh._

object Instance {
  def apply(underlying: aws.model.Instance) = new Instance(underlying)
}

class Instance(underlying: aws.model.Instance) {

  def start()(implicit ec2: EC2) = ec2.startInstances(new aws.model.StartInstancesRequest().withInstanceIds(this.instanceId))

  def stop()(implicit ec2: EC2) = ec2.stopInstances(new aws.model.StopInstancesRequest().withInstanceIds(this.instanceId))

  def terminate()(implicit ec2: EC2) = ec2.terminateInstances(new aws.model.TerminateInstancesRequest().withInstanceIds(this.instanceId))

  def reboot()(implicit ec2: EC2) = ec2.rebootInstances(new aws.model.RebootInstancesRequest().withInstanceIds(this.instanceId))

  protected def provider(keyPairFile: File): HostConfigProvider = new FromStringsHostConfigProvider {
    def rawLines(host: String): com.decodified.scalassh.Validated[(String, TraversableOnce[String])] =
      if (keyPairFile.exists())
        Right("dummy_source" -> (
          Seq("login-type = keyfile",
            "username = ec2-user",
            s"keyfile = ${keyPairFile.getAbsolutePath}",
            "command-timeout = 30000",
            "fingerprint = any" //TODO: ask if user will trust any host key provided by the server. Currently it's always YES.
          )))
      else
        Left(s"KeyFile ${keyPairFile.getAbsolutePath} does not exist")
  }

  def withKeyPair(keyPairFile: File)(f: InstanceWithKeyPair => Unit) = {
    f(InstanceWithKeyPair(underlying, keyPairFile))
  }

  case class InstanceWithKeyPair(private val underlying: aws.model.Instance, keyPairFile: File) extends Instance(underlying) {
    def ssh[T](f: SshClient => SSH.Result[T]) = SSH[T](publicDN, provider(keyPairFile))(f)

    //    override def scp(file: File, kpFile: File = keyPairFile, scpOption: String = "-o StrictHostKeyChecking=no"): Either[String, String] = super.scp(file, keyPairFile, scpOption)
    //
    //    override def process(command: String, kpFile: File = keyPairFile, sshOption: String = "-o StrictHostKeyChecking=no -t -t"): Either[String, String] = super.process(command, keyPairFile, sshOption)
  }

  def ssh[T](f: SshClient => SSH.Result[T], keyPairFile: File) = SSH[T](publicDN, provider(keyPairFile))(f)

  //  def process(command: String, keyPairFile: File, sshOption: String = "-o StrictHostKeyChecking=no -t -t"): Either[String, String] = {
  //    import sys.process._
  //    try {
  //      Right(s"echo ${command}" #&& "echo exit" #> s"ssh ${sshOption} -i ${keyPairFile.getAbsolutePath} ec2-user@${publicDN}" !!)
  //    } catch { case e: Exception => Left(e.toString) }
  //  }
  //
  //  def scp(file: File, keyPairFile: File, scpOption: String = "-o StrictHostKeyChecking=no"): Either[String, String] = {
  //    import sys.process._
  //    try {
  //      Right(s"scp -P 22 ${scpOption} -i ${keyPairFile.getAbsolutePath} ${file.getAbsolutePath} ec2-user@${publicDN}:${file.getName}" !!)
  //    } catch { case e: Exception => Left(e.toString) }
  //  }

  def createImage(imageName: String)(implicit ec2: EC2) = {
    ec2.createImage(new aws.model.CreateImageRequest(instanceId, imageName))
  }

  def getName: Option[String] = tags.get("Name")
  def name: String = tags("Name")

  def instanceId: String = underlying.getInstanceId

  def imageId: String = underlying.getImageId

  def typ: String = underlying.getInstanceType

  def keyName: String = underlying.getKeyName

  def publicDN: String = underlying.getPublicDnsName

  def publicIP: String = underlying.getPublicIpAddress

  def privateDN: String = underlying.getPrivateDnsName

  def privateIP: String = underlying.getPrivateIpAddress

  def tags: Map[String, String] = underlying.getTags.asScala.map(t => t.getKey -> t.getValue).toMap

  def amiLaunchIndex: Int = underlying.getAmiLaunchIndex

  def architecture: String = underlying.getArchitecture

  def blockDeviceMappings: Seq[aws.model.InstanceBlockDeviceMapping] = underlying.getBlockDeviceMappings.asScala

  def clientToken: String = underlying.getClientToken

  def ebsOptimized: Boolean = underlying.getEbsOptimized

  def hypervisor: Option[String] = wrapOption(underlying.getHypervisor)

  def iamInstanceProfile: Option[aws.model.IamInstanceProfile] = wrapOption(underlying.getIamInstanceProfile)

  def getInstanceLifecycle: Option[String] = wrapOption(instanceLifecycle)
  def instanceLifecycle: String = underlying.getInstanceLifecycle

  def kernelId: String = underlying.getKernelId

  def launchTime: Date = underlying.getLaunchTime

  def license: Option[InstanceLicense] = convOption(underlying.getLicense)(InstanceLicense(Instance.this, _))

  def monitoring: aws.model.Monitoring = underlying.getMonitoring

  def networkInterfaces: Seq[aws.model.InstanceNetworkInterface] = underlying.getNetworkInterfaces.asScala

  def placement: aws.model.Placement = underlying.getPlacement

  def getPlatform: Option[String] = wrapOption(platform)
  def platform: String = underlying.getPlatform

  def productCodes: Seq[aws.model.ProductCode] = underlying.getProductCodes.asScala

  def getRamdiskId: Option[String] = wrapOption(ramdiskId)
  def ramdiskId: String = underlying.getRamdiskId

  def rootDeviceName: String = underlying.getRootDeviceName

  def rootDeviceType: String = underlying.getRootDeviceType

  def securityGroups: Seq[aws.model.GroupIdentifier] = underlying.getSecurityGroups.asScala

  def getSpotInstanceRequestId: Option[String] = wrapOption(spotInstanceRequestId)
  def spotInstanceRequestId: String = underlying.getSpotInstanceRequestId

  def state: aws.model.InstanceState = underlying.getState

  def stateReason: Option[aws.model.StateReason] = wrapOption(underlying.getStateReason)

  //this sometimes returns empty string "" but seems not to return null.
  def stateTransitionReason: String = underlying.getStateTransitionReason

  def subnetId: String = underlying.getSubnetId
  def getSubnetId: Option[String] = wrapOption(subnetId)

  def sourceDestCheck: Boolean = underlying.getSourceDestCheck

  def getVirtualizationType: Option[String] = wrapOption(virtualizationType)
  def virtualizationType: String = underlying.getVirtualizationType

  def vpcId: String = underlying.getVpcId
  def getVpcId: Option[String] = wrapOption(vpcId)

  override def toString: String = s"Instance(${underlying.toString})"
}

case class InstanceLicense(instance: Instance, pool: String) extends aws.model.InstanceLicense {
  setPool(pool)
}

object InstanceLicense {
  def apply(instance: Instance, l: aws.model.InstanceLicense): InstanceLicense = InstanceLicense(instance, l.getPool)
}