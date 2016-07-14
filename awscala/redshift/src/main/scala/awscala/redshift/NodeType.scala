package awscala.redshift

object NodeType {
  val dw_hs1_xlarge = NodeType("dw.hs1.xlarge")
  val dw_hs1_8xlarge = NodeType("dw.hs1.8xlarge")
}
case class NodeType(value: String)
