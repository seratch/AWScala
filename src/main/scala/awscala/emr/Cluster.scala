package awscala.emr

import com.amazonaws.services.{ elasticmapreduce => aws }

case class Cluster(in: aws.model.Cluster) extends aws.model.Cluster {
  setApplications(in.getApplications())
  setAutoTerminate(in.getAutoTerminate())
  setEc2InstanceAttributes(in.getEc2InstanceAttributes())
  setId(in.getId())
  setLogUri(in.getLogUri())
  setName(in.getName())
  setRequestedAmiVersion(in.getRequestedAmiVersion())
  setRunningAmiVersion(in.getRunningAmiVersion())
  setStatus(in.getStatus)
  setTags(in.getTags())
  setTerminationProtected(in.getTerminationProtected())
  setVisibleToAllUsers(in.getVisibleToAllUsers())

  private var cachedBootstrapActions: Option[Seq[aws.model.Command]] = None
  private var cachedStepSummares: Option[Seq[aws.model.StepSummary]] = None

  def bootstrapActions(implicit emr: EMR): Seq[aws.model.Command] = {
    cachedBootstrapActions match {
      case None => cachedBootstrapActions = Some(emr.bootstrapActions(Some(getId))); cachedBootstrapActions.get
      case Some(e) => e
    }
  }

  def stepSummaries(implicit emr: EMR): Seq[aws.model.StepSummary] = {
    cachedStepSummares match {
      case None => cachedStepSummares = Some(emr.stepSummaries(Some(getId))); cachedStepSummares.get
      case Some(e) => e
    }
  }
   
  def status(implicit emr: EMR) : aws.model.ClusterStatus = {
    val describeClusterRequest = new aws.model.DescribeClusterRequest().withClusterId(getId)
    emr.describeCluster(describeClusterRequest).getCluster().getStatus()
  }
  
}