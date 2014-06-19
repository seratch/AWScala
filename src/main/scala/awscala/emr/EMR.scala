package awscala.emr

import awscala._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ elasticmapreduce => aws }
import aws.model._

object EMR {
  def apply(credentials: Credentials = CredentialsLoader.load()): EMR = new EMRClient(credentials)
  def apply(accessKeyId: String, secretAccessKey: String): EMR = apply(Credentials(accessKeyId, secretAccessKey))
  def at(region: Region): EMR = apply().at(region)
}

trait EMR extends aws.AmazonElasticMapReduce {

  final val masterGroupName: String = "Master"
  final val masterInstanceRoleType = InstanceRoleType.MASTER
  final val coreGroupName: String = "Core"
  final val coreInstanceRoleType = InstanceRoleType.CORE
  final val taskGroupName: String = "Task"
  final val taskInstanceRoleType = InstanceRoleType.TASK

  lazy val CHECK_INTERVAL = 5000L

  def at(region: Region): EMR = {
    this.setRegion(region)
    this
  }

  def buildMasterGroupConfig(masterInstanceType: String, masterMarketType: String, masterBidPrice: String = "0.0"): aws.model.InstanceGroupConfig = {
    val masterMarketTypeObject = MarketType.fromValue(masterMarketType)

    //building master node
    val masterGroupConfig = new InstanceGroupConfig()
      .withName(masterGroupName)
      .withInstanceRole(masterInstanceRoleType)
      .withInstanceCount(1)
      .withInstanceType(masterInstanceType)
      .withMarket(masterMarketTypeObject)

    //build master market type
    if (masterMarketTypeObject.name() == "SPOT") masterGroupConfig.withBidPrice(masterBidPrice)
    masterGroupConfig
  }

  def buildCoreGroupConfig(coreInstanceType: String, coreInstanceCount: Int, coreMarketType: String, coreBidPrice: String = "0.0"): aws.model.InstanceGroupConfig = {
    val corerMarketTypeObject = MarketType.fromValue(coreMarketType)
    val coreGroupConfig = new InstanceGroupConfig()
      .withName(coreGroupName)
      .withInstanceRole(coreInstanceRoleType)
      .withInstanceType(coreInstanceType)
      .withInstanceCount(coreInstanceCount)
      .withMarket(corerMarketTypeObject)
    //build core market type
    if (corerMarketTypeObject.name() == "SPOT") coreGroupConfig.withBidPrice(coreBidPrice)
    coreGroupConfig
  }

  def buildTaskGroupConfig(taskInstanceType: String, taskInstanceCount: Int, taskMarketType: String, taskBidPrice: String = "0.0"): aws.model.InstanceGroupConfig = {
    val taskMarketTypeObject = MarketType.fromValue(taskMarketType)
    val taskGroupConfig = new InstanceGroupConfig()
      .withName(taskGroupName)
      .withInstanceRole(taskInstanceRoleType)
      .withInstanceType(taskInstanceType)
      .withInstanceCount(taskInstanceCount)
      .withMarket(taskMarketTypeObject)
    //build task market type
    if (taskMarketTypeObject.name() == "SPOT") taskGroupConfig.withBidPrice(taskBidPrice)
    taskGroupConfig
  }

  //Step #1
  def buildJobFlowInstancesConfig(
    masterInstanceType: String = "m1.small",
    masterMarketType: String = "ON_DEMAND",
    masterBidPrice: String = "0.0",
    coreInstanceType: String = "m1.small",
    coreInstanceCount: Int = 1,
    coreMarketType: String = "ON_DEMAND",
    coreBidPrice: String = "0.0",
    taskInstanceType: String = "m1.small",
    taskInstanceCount: Int = 1,
    taskMarketType: String = "ON_DEMAND",
    taskBidPrice: String = "0.0",
    ec2KeyName: String,
    hadoopVersion: String): JobFlowInstancesConfig = {

    //building master node
    val masterGroupConfig = buildMasterGroupConfig(masterInstanceType, masterMarketType, masterBidPrice)
    //building core node
    val coreGroupConfig = buildCoreGroupConfig(coreInstanceType, coreInstanceCount, coreMarketType, coreBidPrice)
    //building task node
    val taskGroupConfig = buildTaskGroupConfig(taskInstanceType, taskInstanceCount, taskMarketType, taskBidPrice)
    val clusterGroups = List(masterGroupConfig, coreGroupConfig, taskGroupConfig)
    val addInstanceGroupsRequest = new AddInstanceGroupsRequest().withInstanceGroups(clusterGroups)
    new JobFlowInstancesConfig()
      .withEc2KeyName(ec2KeyName)
      .withHadoopVersion(hadoopVersion)
      .withInstanceGroups(addInstanceGroupsRequest.getInstanceGroups())
  }

  //Step #2
  def buildJobFlowStepsRequest[T](steps: List[T], jobFlowId: String = ""): AddJobFlowStepsRequest = {
    val stepConfig = buildSteps(steps.asInstanceOf[List[jarStep]])
    val addJobFlowStepsRequest = new AddJobFlowStepsRequest().withSteps(stepConfig)
    if (jobFlowId != "") addJobFlowStepsRequest.withJobFlowId(jobFlowId)

    addJobFlowStepsRequest
  }

  case class jarStep(stepName: String, stepType: String, stepPath: String, stepClass: String, stepArgs: List[String])

  private def buildSteps(steps: List[jarStep]): List[aws.model.StepConfig] = {
    for {
      step <- steps
      aStepConfigJar = new HadoopJarStepConfig(step.stepPath)
        .withMainClass(step.stepClass)
        .withArgs(step.stepArgs)

      aStepConfig = new StepConfig()
        .withName(step.stepName)
        .withHadoopJarStep(aStepConfigJar)
    } yield {
      aStepConfig
    }

  }

  //Step #3
  def buildRunRequest(
    jobName: String = "AWSscala",
    amiVersion: String = "latest",
    loggingURI: String = "",
    visibleToAllUsers: Boolean = true,
    jobFlowInstancesConfig: JobFlowInstancesConfig,
    jobFlowStepsRequest: AddJobFlowStepsRequest): RunJobFlowRequest = {
    new RunJobFlowRequest()
      .withName(jobName)
      .withAmiVersion(amiVersion)
      .withSteps(jobFlowStepsRequest.getSteps())
      .withLogUri(loggingURI)
      .withVisibleToAllUsers(visibleToAllUsers)
      .withInstances(jobFlowInstancesConfig)
  }

  def runJobFlow[T](
    masterInstanceType: String = "m1.small",
    masterMarketType: String = "ON_DEMAND",
    masterBidPrice: String = "0.0",
    coreInstanceType: String = "m1.small",
    coreInstanceCount: Int = 1,
    coreMarketType: String = "ON_DEMAND",
    coreBidPrice: String = "0.0",
    taskInstanceType: String = "m1.small",
    taskInstanceCount: Int = 1,
    taskMarketType: String = "ON_DEMAND",
    taskBidPrice: String = "0.0",
    ec2KeyName: String,
    hadoopVersion: String,
    steps: List[T],
    jobFlowId: String = "",
    jobName: String = "AWSscala",
    amiVersion: String = "latest",
    loggingURI: String = "",
    visibleToAllUsers: Boolean = true): aws.model.RunJobFlowResult = {

    val jobFlowInstancesConfig = buildJobFlowInstancesConfig(
      masterInstanceType,
      masterMarketType,
      masterBidPrice,
      coreInstanceType,
      coreInstanceCount,
      coreMarketType,
      coreBidPrice,
      taskInstanceType,
      taskInstanceCount,
      taskMarketType,
      taskBidPrice,
      ec2KeyName,
      hadoopVersion)

    val jobFlowStepsRequest = buildJobFlowStepsRequest[T](
      steps,
      jobFlowId)

    val runJobFlowRequest = buildRunRequest(
      jobName,
      amiVersion,
      loggingURI,
      visibleToAllUsers,
      jobFlowInstancesConfig,
      jobFlowStepsRequest)

    runJobFlow(runJobFlowRequest)
  }
  
  def clusters(clusterStates: Seq[String] = Nil, createdBefore: Option[java.util.Date] = None, createdAfter: Option[java.util.Date] = None): Seq[Cluster] =
    clusterSummaries(clusterStates, createdBefore, createdAfter) map { x => Cluster(x.getId())}

  def clusterSummaries(clusterStates: Seq[String] = Nil, createdBefore: Option[java.util.Date] = None, createdAfter: Option[java.util.Date] = None): Seq[ClusterSummary] = {
    import aws.model.ListClustersResult
    object clustersSequencer extends Sequencer[ClusterSummary, ListClustersResult, String] {
      val base = new ListClustersRequest().withClusterStates(clusterStates.toList.asJava)
      val baseRequest1 = if (createdBefore == None) base else base.withCreatedBefore(createdBefore.get)
      val baseRequest2 = if (createdAfter == None) baseRequest1 else baseRequest1.withCreatedAfter(createdAfter.get)
      def getInitial = listClusters(baseRequest2)
      def getMarker(r: ListClustersResult) = r.getMarker()
      def getFromMarker(marker: String) = listClusters(baseRequest2.withMarker(marker))
      def getList(r: ListClustersResult) = r.getClusters()
    }
    clustersSequencer.sequence
  }

  def bootstrapActions(clusterId: Option[String] = None): Seq[Command] = {
    import aws.model.ListBootstrapActionsResult
    object actionSequencer extends Sequencer[Command, ListBootstrapActionsResult, String] {
      val baseRequest = if (clusterId == None) new ListBootstrapActionsRequest() else new ListBootstrapActionsRequest().withClusterId(clusterId.get)
      def getInitial = listBootstrapActions(baseRequest)
      def getMarker(r: ListBootstrapActionsResult) = r.getMarker()
      def getFromMarker(marker: String) = listBootstrapActions(baseRequest.withMarker(marker))
      def getList(r: ListBootstrapActionsResult) = r.getBootstrapActions()
    }
    actionSequencer.sequence
  }

  def stepSummaries(clusterId: Option[String] = None, stepStates: Seq[String] = Nil): Seq[StepSummary] = {
    import aws.model.ListStepsResult
    object stepsSequencer extends Sequencer[StepSummary, ListStepsResult, String] {
      val base = if (clusterId == None) new ListStepsRequest() else new ListStepsRequest().withClusterId(clusterId.get)
      val baseRequest = base.withStepStates(stepStates.toList.asJava)
      def getInitial = listSteps(baseRequest)
      def getMarker(r: ListStepsResult) = r.getMarker()
      def getFromMarker(marker: String) = listSteps(baseRequest.withMarker(marker))
      def getList(r: ListStepsResult) = r.getSteps()
    }
    stepsSequencer.sequence
  }

  def getClusterDetail[T](jobFlowId: String, op: aws.model.Cluster => T): T = {
    val describeClusterRequest = new DescribeClusterRequest().withClusterId(jobFlowId)
    val cluster = describeCluster(describeClusterRequest).getCluster()
    op(cluster)
  }

  def getClusterState(jobFlowId: String): String = {
    def getState(cluster: aws.model.Cluster): String = cluster.getStatus().getState()
    getClusterDetail(jobFlowId, getState)
  }

  def getClusterName(jobFlowId: String): String = {
    def getName(cluster: aws.model.Cluster): String = cluster.getName()
    getClusterDetail(jobFlowId, getName)
  }

  def terminateCluster(jobFlowId: String): Unit = terminateJobFlows(new TerminateJobFlowsRequest().withJobFlowIds(jobFlowId))

}

class EMRClient(credentials: Credentials = CredentialsLoader.load()) extends aws.AmazonElasticMapReduceClient(credentials) with EMR
