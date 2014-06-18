package awscala.emr

import com.amazonaws.services.{ elasticmapreduce => aws }

case class Cluster(id: String) {
  def name(implicit emr: EMR): String = emr.getClusterName(id)
  def state(implicit emr: EMR): String = emr.getClusterState(id)
  def bootstrapActions(implicit emr: EMR) : Seq[aws.model.Command] = emr.bootstrapActions(Some(id))
  def steps(implicit emr: EMR) : Seq[aws.model.StepSummary] = emr.steps(Some(id))
  def terminate(implicit emr: EMR) = emr.terminateCluster(id)
}