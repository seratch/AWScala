package awscala.emr

import awscala._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ elasticmapreduce => aws }
import com.amazonaws.services.elasticmapreduce.model._

object EMR {
  def apply(credentials: Credentials = Credentials.defaultEnv): EMR = new EMRClient(credentials)
  def apply(accessKeyId: String, secretAccessKey: String): EMR = apply(Credentials(accessKeyId, secretAccessKey))
  def at(region: Region): EMR = apply().at(region)
}

trait EMR extends aws.AmazonElasticMapReduce {

  lazy val CHECK_INTERVAL = 5000L

  def at(region: Region): EMR = {
    this.setRegion(region)
    this
  }

  def buildMasterGroupConfig(masterInstanceType: String, masterMarketType: String, masterBidPrice: String = "0.0"): com.amazonaws.services.elasticmapreduce.model.InstanceGroupConfig =
    {
      //building master node
      val masterGroupConfig = new InstanceGroupConfig()
        .withName("Master")
        .withInstanceRole("MASTER")
        .withInstanceCount(1)
        .withInstanceType(masterInstanceType)
        .withMarket(masterMarketType)

      //build master market type
      if (masterMarketType == "SPOT") masterGroupConfig.withBidPrice(masterBidPrice)
      masterGroupConfig
    }

  def buildCoreGroupConfig(coreInstanceType: String, coreInstanceCount: Int, coreMarketType: String, coreBidPrice: String = "0.0"): com.amazonaws.services.elasticmapreduce.model.InstanceGroupConfig =
    {
      val coreGroupConfig = new InstanceGroupConfig()
        .withName("CORE")
        .withInstanceRole("CORE")
        .withInstanceType(coreInstanceType)
        .withInstanceCount(coreInstanceCount)
        .withMarket(coreMarketType)
      //build core market type
      if (coreMarketType == "SPOT") coreGroupConfig.withBidPrice(coreBidPrice)
      coreGroupConfig
    }

  def buildTaskGroupConfig(taskInstanceType: String, taskInstanceCount: Int, taskMarketType: String, taskBidPrice: String = "0.0"): com.amazonaws.services.elasticmapreduce.model.InstanceGroupConfig =
    {
      val taskGroupConfig = new InstanceGroupConfig()
        .withName("TASK")
        .withInstanceRole("TASK")
        .withInstanceType(taskInstanceType)
        .withInstanceCount(taskInstanceCount)
        .withMarket(taskMarketType)
      //build task market type
      if (taskMarketType == "SPOT") taskGroupConfig.withBidPrice(taskBidPrice)
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
    hadoopVersion: String): JobFlowInstancesConfig =
    {

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
  def buildJobFlowStepsRequest[T](steps: List[T], jobFlowId: String = ""): AddJobFlowStepsRequest =
    {
      val stepConfig = buildSteps(steps.asInstanceOf[List[jarStep]])
      val addJobFlowStepsRequest = new AddJobFlowStepsRequest().withSteps(stepConfig)
      if (jobFlowId != "")
        addJobFlowStepsRequest.withJobFlowId(jobFlowId)

      addJobFlowStepsRequest

    }

  case class jarStep(stepName: String, stepType: String, stepPath: String, stepClass: String, stepArgs: List[String])

  private def buildSteps(steps: List[jarStep]): List[com.amazonaws.services.elasticmapreduce.model.StepConfig] =
    {

      for {
        step <- steps
        val aStepConfigJar = new HadoopJarStepConfig(step.stepPath)
          .withMainClass(step.stepClass)
          .withArgs(step.stepArgs)

        val aStepConfig = new StepConfig()
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
    jobFlowStepsRequest: AddJobFlowStepsRequest): RunJobFlowRequest =
    {
      new RunJobFlowRequest()
        .withName(jobName)
        .withAmiVersion(amiVersion)
        .withSteps(jobFlowStepsRequest.getSteps())
        .withLogUri(loggingURI)
        .withVisibleToAllUsers(visibleToAllUsers)
        .withInstances(jobFlowInstancesConfig)

    }

  // format: OFF
  
  def runJobFlow[T](
    masterInstanceType	    : String = "m1.small",
    masterMarketType	    : String = "ON_DEMAND",
    masterBidPrice		    : String = "0.0",
    coreInstanceType	    : String = "m1.small",
    coreInstanceCount	    : Int	 = 1 ,
    coreMarketType		    : String = "ON_DEMAND",
    coreBidPrice		    : String = "0.0",
    taskInstanceType	    : String = "m1.small",
    taskInstanceCount	    : Int	 = 1  ,
    taskMarketType		    : String = "ON_DEMAND",
    taskBidPrice		    : String = "0.0",
    ec2KeyName			    : String,
    hadoopVersion		    : String,
    steps				    : List[T],
    jobFlowId			    : String = "",
    jobName				    : String = "AWSscala",
    amiVersion			    : String = "latest",
    loggingURI			    : String = "",
    visibleToAllUsers	    : Boolean = true 
    ):  com.amazonaws.services.elasticmapreduce.model.RunJobFlowResult  =
    {

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

  // format: ON

  def getClusterDetail[T](jobFlowId: String, op: com.amazonaws.services.elasticmapreduce.model.Cluster => T): T =
    {

      val describeClusterRequest = new DescribeClusterRequest().withClusterId(jobFlowId)
      val cluster = describeCluster(describeClusterRequest).getCluster()
      op(cluster)

    }

  def getClusterState(jobFlowId: String): String =
    {
      def getState(cluster: com.amazonaws.services.elasticmapreduce.model.Cluster): String = cluster.getStatus().getState()
      getClusterDetail(jobFlowId, getState)

    }

  def getClusterName(jobFlowId: String): String =
    {
      def getName(cluster: com.amazonaws.services.elasticmapreduce.model.Cluster): String = cluster.getName()
      getClusterDetail(jobFlowId, getName)

    }

  def TerminateCluster(jobFlowId: String) =
    {
      new TerminateJobFlowsRequest().withJobFlowIds(jobFlowId).getJobFlowIds().get(0)

    }

}

class EMRClient(credentials: Credentials = Credentials.defaultEnv) extends aws.AmazonElasticMapReduceClient(credentials) with EMR
