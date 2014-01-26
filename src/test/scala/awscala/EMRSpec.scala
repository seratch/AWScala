package awscala.emr

import org.slf4j._
import org.scalatest._
import org.scalatest.matchers._
import awscala._
import scala.collection.JavaConversions._

class EMRSpec extends FlatSpec with ShouldMatchers {
  behavior of "EMR"
 
  implicit val emr = EMR.at(Region.US_EAST_1)
   val log = LoggerFactory.getLogger(this.getClass)
 
  /**
   * starts an EMR cluster based on "on-demand" instances
   */

  it should "cluster configurations on demand" in {
        

    
    //cluster nodes information
    val masterInstanceType = "c1.medium"
    val masterMarketType = "ON_DEMAND"
    val masterBidPrice = "0.00"
    val coreInstanceType = "c1.medium"
    val coreInstanceCount = 1
    val coreMarketType = "ON_DEMAND"
    val coreBidPrice = "0.00"
    val taskInstanceType = "c1.medium"
    val taskInstanceCount = 1
    val taskMarketType = "ON_DEMAND"
    val taskBidPrice = "0.00"
    val ec2KeyName = "ec2KeyName"
    val hadoopVersion = "1.0.3"
    //job settings
    val jobName = "My Test Job"
    val amiVersion = "latest"
    val loggingURI = "s3://path/"
    val visibleToAllUsers = true
    //individual steps information      
    val step1 = emr.jarStep("step1", "jarStep", "s3://path", "com.myclass", List("--key1", "value1", "--key2", "value2"))
    val step2 = emr.jarStep("step2", "jarStep", "s3://path", "com.myclass", List("--key1", "value1", "--key2", "value2"))
    val steps = List(step1, step2)
    val jobFlowInstancesConfig = emr.buildJobFlowInstancesConfig(masterInstanceType, masterMarketType, masterBidPrice, coreInstanceType, coreInstanceCount, coreMarketType, coreBidPrice, taskInstanceType, taskInstanceCount, taskMarketType, taskBidPrice, ec2KeyName, hadoopVersion)
    val master = jobFlowInstancesConfig.getInstanceGroups()(0)
    val core = jobFlowInstancesConfig.getInstanceGroups()(1)
    val task = jobFlowInstancesConfig.getInstanceGroups()(2)

    // test for node configuration
    master.toString() should equal("{Name: Master,Market: ON_DEMAND,InstanceRole: MASTER,InstanceType: c1.medium,InstanceCount: 1}")
    core.toString() should equal("{Name: CORE,Market: ON_DEMAND,InstanceRole: CORE,InstanceType: c1.medium,InstanceCount: 1}")
    task.toString() should equal("{Name: TASK,Market: ON_DEMAND,InstanceRole: TASK,InstanceType: c1.medium,InstanceCount: 1}")

    // test for general cluster configuration
    jobFlowInstancesConfig.getEc2KeyName() should equal(ec2KeyName)
    jobFlowInstancesConfig.getHadoopVersion() should equal(hadoopVersion)

    // to add steps to new server
    val jobFlowStepsRequest = emr.buildJobFlowStepsRequest(steps)

    // to add steps to an existing server
    //val jobFlowStepsRequest2 = emr.buildJobFlowStepsRequest(steps ,jobFlowId="j-XXXXXXXXXXX")

    val steps_test_list = jobFlowStepsRequest.getSteps()

    for (i <- 0 to steps_test_list.size() - 1) {
      //test for steps configurtaion 
      steps_test_list.get(i).getName() should equal(steps.get(i).stepName)
      steps_test_list.get(i).getHadoopJarStep().getArgs().toSeq should equal(steps.get(i).stepArgs)
      steps_test_list.get(i).getHadoopJarStep().getMainClass() should equal(steps.get(i).stepClass)
      steps_test_list.get(i).getHadoopJarStep().getJar() should equal(steps.get(i).stepPath)

    }

    val runJobFlowRequest = emr.buildRunRequest(jobName, amiVersion, loggingURI, visibleToAllUsers, jobFlowInstancesConfig, jobFlowStepsRequest)
   //uncomment to add steps on the server.  
   /* 
    val runJobFlowResult = emr.runJobFlow(runJobFlowRequest)
    val job_flow_id = runJobFlowResult.getJobFlowId()
     
    */

  }

  it should "cluster configurations SPOT" in {
    //cluster nodes information
    val masterInstanceType = "cc2.8xlarge"
    val masterMarketType = "SPOT"
    val masterBidPrice = "2.10"
    val coreInstanceType = "c1.small"
    val coreInstanceCount = 1
    val coreMarketType = "SPOT"
    val coreBidPrice = "3.00"
    val taskInstanceType = "c1.xlarge"
    val taskInstanceCount = 1
    val taskMarketType = "SPOT"
    val taskBidPrice = "1.50"
    val ec2KeyName = "ec2KeyName"
    val hadoopVersion = "1.0.3"

    //job settings
    val jobName = "cluster configurations SPOT"
    val amiVersion = "latest"
    val loggingURI = "s3://path to my logging bucket"
    val visibleToAllUsers = true

    //individual steps information      
    val step1 = emr.jarStep("step1", "jarStep", "s3://path", "com.myclass", List("--key1", "value1", "--key2", "value2"))
    val step2 = emr.jarStep("step2", "jarStep", "s3://path", "com.myclass", List("--key1", "value1", "--key2", "value2"))
    val steps = List(step1, step2)

    val jobFlowInstancesConfig = emr.buildJobFlowInstancesConfig(masterInstanceType, masterMarketType, masterBidPrice, coreInstanceType, coreInstanceCount, coreMarketType, coreBidPrice, taskInstanceType, taskInstanceCount, taskMarketType, taskBidPrice, ec2KeyName, hadoopVersion)

    val master = jobFlowInstancesConfig.getInstanceGroups()(0)
    val core = jobFlowInstancesConfig.getInstanceGroups()(1)
    val task = jobFlowInstancesConfig.getInstanceGroups()(2)

    master.toString() should equal("{Name: Master,Market: SPOT,InstanceRole: MASTER,BidPrice: 2.10,InstanceType: cc2.8xlarge,InstanceCount: 1}")
    core.toString() should equal("{Name: CORE,Market: SPOT,InstanceRole: CORE,BidPrice: 3.00,InstanceType: c1.small,InstanceCount: 1}")
    task.toString() should equal("{Name: TASK,Market: SPOT,InstanceRole: TASK,BidPrice: 1.50,InstanceType: c1.xlarge,InstanceCount: 1}")

    val jobFlowStepsRequest = emr.buildJobFlowStepsRequest(steps)
    val steps_test_list = jobFlowStepsRequest.getSteps()

    for (i <- 0 to steps_test_list.size() - 1) {
      steps_test_list.get(i).getName() should equal(steps.get(i).stepName)
      steps_test_list.get(i).getHadoopJarStep().getArgs().toSeq should equal(steps.get(i).stepArgs)
      steps_test_list.get(i).getHadoopJarStep().getMainClass() should equal(steps.get(i).stepClass)
      steps_test_list.get(i).getHadoopJarStep().getJar() should equal(steps.get(i).stepPath)

    }

  }

  it should "run cluster with one method call" in
    {
      //cluster nodes information
      val masterInstanceType = "c1.medium"
      val masterMarketType = "ON_DEMAND"
      val masterBidPrice = "0.00"
      val coreInstanceType = "c1.medium"
      val coreInstanceCount = 1
      val coreMarketType = "ON_DEMAND"
      val coreBidPrice = "0.00"
      val taskInstanceType = "c1.medium"
      val taskInstanceCount = 1
      val taskMarketType = "ON_DEMAND"
      val taskBidPrice = "0.00"
      val ec2KeyName = "ec2KeyName"
      val hadoopVersion = "1.0.3"
      //job settings
      val jobName = "Test one run method"
      val amiVersion = "latest"
      val loggingURI = "s3://path/"
      val visibleToAllUsers = true
      //individual steps information      
      val step1 = emr.jarStep("step1", "jarStep", "s3://path", "com.myclass", List("--key1", "value1", "--key2", "value2"))
      val step2 = emr.jarStep("step2", "jarStep", "s3://path", "com.myclass", List("--key1", "value1", "--key2", "value2"))
      val steps = List(step1, step2)

      val run_request = emr.runJobFlow(masterInstanceType, masterMarketType, masterBidPrice, coreInstanceType, coreInstanceCount, coreMarketType, coreBidPrice, taskInstanceType, taskInstanceCount, taskMarketType, taskBidPrice, ec2KeyName, hadoopVersion, steps, "", jobName, amiVersion, loggingURI, visibleToAllUsers)

      val job_flow_id = run_request.getJobFlowId()
      log.info(s"Created cluster with job flow id = $job_flow_id")
      Thread.sleep(10000)
      var state = emr.getClusterState(job_flow_id)
      log.info(s" current state of cluster is $state")

      state should equal("STARTING")
      val response_jobFlowId = emr.terminateCluster(job_flow_id)
      state should equal("TERMINATED")

    }

  it should "cluster shutdown" in {
    val jobFlowId = "j-12CU6XBCMQ2TP"
    val response_jobFlowId = emr.terminateCluster(jobFlowId)

    jobFlowId should equal(response_jobFlowId)

  }

  it should "cluster status" in
    {
      val jobFlowId = "j-3B6BS0TV2NVN9"
      val state = emr.getClusterState(jobFlowId)
      state should equal("TERMINATED")
    }

  it should "custom define cluster information" in
    {
      val jobFlowId = "j-12CU6XBCMQ2TP"
      def getClusterName(cluster: com.amazonaws.services.elasticmapreduce.model.Cluster): String = cluster.getName()

      val ami_version = emr.getClusterDetail(jobFlowId, getClusterName)
      ami_version should equal("country report")
    }

}
