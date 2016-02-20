package awscala.redshift

import scala.collection.JavaConverters._
import com.amazonaws.services.{ redshift => aws }

object ClusterSubnetGroup {

  def apply(g: aws.model.ClusterSubnetGroup): ClusterSubnetGroup = new ClusterSubnetGroup(
    name = g.getClusterSubnetGroupName,
    description = g.getDescription,
    status = g.getSubnetGroupStatus,
    subnets = g.getSubnets.asScala.map(s => Subnet(s)).toSeq,
    vpcId = g.getVpcId
  )
}

case class ClusterSubnetGroup(
  name: String, description: String, status: String, subnets: Seq[Subnet], vpcId: String
)
    extends aws.model.ClusterSubnetGroup {

  setClusterSubnetGroupName(name)
  setDescription(description)
  setSubnetGroupStatus(status)
  setSubnets(subnets.map(_.asInstanceOf[aws.model.Subnet]).asJava)
  setVpcId(vpcId)

  def destroy()(implicit redshift: Redshift) = redshift.delete(this)
}
