package awscala.emr

import com.amazonaws.services.{ elasticmapreduce => aws }
import scala.collection.JavaConverters._

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
  private var cachedStepSummaries: Option[Seq[aws.model.StepSummary]] = None

  def name = getName()
  def id = getId()
  lazy val applications = getApplications().asScala
  def autoTerminate = getAutoTerminate()
  def logUri = getLogUri()
  def requestedAmiVersion = getRequestedAmiVersion()
  def runningAmiVersion = getRunningAmiVersion()
  lazy val tags = getTags().asScala
  def terminationProtected = getTerminationProtected()
  def visibleToAllUsers = getVisibleToAllUsers()

  def bootstrapActions(implicit emr: EMR): Seq[aws.model.Command] = {
    cachedBootstrapActions match {
      case None =>
        cachedBootstrapActions = Some(emr.bootstrapActions(Some(getId)))
        cachedBootstrapActions.get
      case Some(e) => e
    }
  }

  def stepSummaries(implicit emr: EMR): Seq[aws.model.StepSummary] = {
    cachedStepSummaries match {
      case None =>
        cachedStepSummaries = Some(emr.stepSummaries(Some(getId)))
        cachedStepSummaries.get
      case Some(e) => e
    }
  }

  def status(implicit emr: EMR): aws.model.ClusterStatus = {
    val describeClusterRequest = new aws.model.DescribeClusterRequest().withClusterId(getId)
    emr.describeCluster(describeClusterRequest).getCluster().getStatus()
  }

}
