package awscala.redshift

import com.amazonaws.services.{ redshift => aws }

object ClusterParameterGroup {
  def apply(g: aws.model.ClusterParameterGroup): ClusterParameterGroup = new ClusterParameterGroup(
    name = g.getParameterGroupName,
    family = g.getParameterGroupFamily,
    description = g.getDescription)
}
case class ClusterParameterGroup(name: String, family: String, description: String) extends aws.model.ClusterParameterGroup {
  setParameterGroupName(name)
  setParameterGroupFamily(family)
  setDescription(description)

  def destroy()(implicit redshift: Redshift) = redshift.delete(this)

}
