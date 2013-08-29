package awscala.redshift

import com.amazonaws.services.{redshift => aws}

object ClusterVersion {
  val Version_1_0 = ClusterVersion("1.0")

  def apply(v: aws.model.ClusterVersion): ClusterVersion = new ClusterVersion(
    version = v.getClusterVersion,
    description = Option(v.getDescription),
    parameterGroupFamily = Option(v.getClusterParameterGroupFamily)
  )
}

case class ClusterVersion(
 version: String, description: Option[String] = None,  parameterGroupFamily: Option[String] = None)
  extends aws.model.ClusterVersion {

  setClusterVersion(version)
  setDescription(description.orNull[String])
  setClusterParameterGroupFamily(parameterGroupFamily.orNull[String])
}
