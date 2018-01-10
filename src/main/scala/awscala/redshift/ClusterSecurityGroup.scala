package awscala.redshift

import scala.collection.JavaConverters._
import com.amazonaws.services.{ redshift => aws }

object ClusterSecurityGroup {
  def apply(g: aws.model.ClusterSecurityGroup): ClusterSecurityGroup = new ClusterSecurityGroup(
    name = g.getClusterSecurityGroupName,
    ec2SecurityGroups = g.getEC2SecurityGroups.asScala.map(g =>
      EC2SecurityGroup(g.getEC2SecurityGroupName, g.getEC2SecurityGroupOwnerId, g.getStatus)).toSeq,
    ipranges = g.getIPRanges.asScala.map(i => IPRange(i.getCIDRIP, i.getStatus)).toSeq,
    description = g.getDescription)
}
case class ClusterSecurityGroup(
  name: String, ec2SecurityGroups: Seq[EC2SecurityGroup], ipranges: Seq[IPRange], description: String)
  extends aws.model.ClusterSecurityGroup {

  setClusterSecurityGroupName(name)
  setDescription(description)
  setEC2SecurityGroups(ec2SecurityGroups.map(_.asInstanceOf[aws.model.EC2SecurityGroup]).asJava)
  setIPRanges(ipranges.map(_.asInstanceOf[aws.model.IPRange]).asJava)

  def destroy()(implicit redshift: Redshift) = redshift.delete(this)
}

