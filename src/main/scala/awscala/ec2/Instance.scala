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

  protected def provider(keyPairFile: File, trustAnyHostKey: Boolean = false): HostConfigProvider = new FromStringsHostConfigProvider {
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

    def scp(file: File) = {
      import sys.process._
      //TODO: ask if user will trust any host key provided by the server. Currently it's always YES.
      s"scp -P 22 -o StrictHostKeyChecking=no -i ${keyPairFile.getAbsolutePath} ${file.getAbsolutePath}} ec2-user@${publicDN}:~/${file.getName}" !!
    }
  }

  def ssh[T](f: SshClient => SSH.Result[T], keyPairFile: File) = SSH[T](publicDN, provider(keyPairFile))(f)

  //  def process(command: String, keyPairFile: File):(String,Instance) = {
  //    import sys.process._
  //    val result = s"echo ${command}" #&& "echo exit" #> s"ssh  -o StrictHostKeyChecking=no -t -t -i ${keyPairFile.getAbsolutePath} ec2-user@${publicDN}" !!
  //
  //    (result,this)
  //  }
  //

  def scp(file: File, keyPairFile: File) = {
    import sys.process._
    s"scp -P 22 -o StrictHostKeyChecking=no -i ${keyPairFile.getAbsolutePath} ${file.getAbsolutePath}} ec2-user@${publicDN}:~/${file.getName}" !!
  }

  def createImage(imageName: String)(implicit ec2: EC2) = {
    ec2.createImage(new aws.model.CreateImageRequest(instanceId, imageName))
  }

  def name: Option[String] = tags.get("Name")

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

  def instanceLifecycle: Option[String] = wrapOption(underlying.getInstanceLifecycle)

  def kernelId: String = underlying.getKernelId

  def launchTime: Date = underlying.getLaunchTime

  def license: Option[InstanceLicense] = convOption(underlying.getLicense)(InstanceLicense(Instance.this, _))

  def monitoring: aws.model.Monitoring = underlying.getMonitoring

  def networkInterfaces: Seq[aws.model.InstanceNetworkInterface] = underlying.getNetworkInterfaces.asScala

  def placement: aws.model.Placement = underlying.getPlacement

  def platform: Option[String] = wrapOption(underlying.getPlatform)

  def productCodes: Seq[aws.model.ProductCode] = underlying.getProductCodes.asScala

  def ramdiskId: Option[String] = wrapOption(underlying.getRamdiskId)

  def rootDeviceName: String = underlying.getRootDeviceName

  def rootDeviceType: String = underlying.getRootDeviceType

  def securityGroups: Seq[aws.model.GroupIdentifier] = underlying.getSecurityGroups.asScala

  def spotInstanceRequestId: Option[String] = wrapOption(underlying.getSpotInstanceRequestId)

  def state: aws.model.InstanceState = underlying.getState

  def stateReason: Option[aws.model.StateReason] = wrapOption(underlying.getStateReason)

  //this sometimes returns empty string "" but seems not to return null.
  def stateTransitionReason: String = underlying.getStateTransitionReason

  def subnetId: Option[String] = wrapOption(underlying.getSubnetId)

  def sourceDestCheck: Boolean = underlying.getSourceDestCheck

  def virtualizationType: Option[String] = wrapOption(underlying.getVirtualizationType)

  def vpcId: Option[String] = wrapOption(underlying.getVpcId)

  private def wrapOption[A](value: A): Option[A] = if (null != value) Some(value) else None

  private def convOption[A, B](value: A)(conv: A => B): Option[B] = if (null != value) Some(conv(value)) else None
}

case class InstanceLicense(instance: Instance, pool: String) extends aws.model.InstanceLicense {
  setPool(pool)
}

object InstanceLicense {
  def apply(instance: Instance, l: aws.model.InstanceLicense): InstanceLicense = InstanceLicense(instance, l.getPool)
}