package awscala.redshift

object ClusterType {
  val SingleNode = ClusterType("single-node")
  val MultiNode = ClusterType("multi-node")
}
case class ClusterType(name: String)
