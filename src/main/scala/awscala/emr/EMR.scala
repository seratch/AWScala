package awscala.emr

import awscala._
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ elasticmapreduce => aws }
import com.amazonaws.services.elasticmapreduce._
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

    def buildEMRCluster(masterInstanceType 	:String, 
    					masterMarketType	:String, 
    					masterBidPrice 		:String ="0.0" ,
    					coreInstanceType	:String ,
    					coreInstanceCount	:Int,
    					coreMarketType		:String , 
    					coreBidPrice 		:String ="0.0" ,
    					taskInstanceType	:String ,
    					taskInstanceCount	:Int,
    					taskMarketType		:String , 
    					taskBidPrice 		:String ="0.0" ): AmazonElasticMapReduceClient =
    {
      
      
      //building master node
      val masterGroupConfig = new InstanceGroupConfig()
      	  .withName("Master")
      	  .withInstanceRole("MASTER")
      	  .withInstanceCount(1)
          .withInstanceType(masterInstanceType)
          .withMarket(masterMarketType)
      //build master market type
      if (masterMarketType=="SPOT")	  masterGroupConfig.withBidPrice(masterBidPrice)
         
      
      //building core node
       val coreGroupConfig   = new InstanceGroupConfig()
      	  .withName("CORE")
      	  .withInstanceRole("CORE")
      	  .withInstanceType(coreInstanceType)
      	  .withInstanceCount(coreInstanceCount)
          .withMarket(coreMarketType)
      //build core market type
      if (coreMarketType=="SPOT") coreGroupConfig.withBidPrice(coreBidPrice)
       
      //building task node
       val taskGroupConfig   = new InstanceGroupConfig()
      	  .withName("TASK")
      	  .withInstanceRole("TASK")
      	  .withInstanceType(taskInstanceType)
      	  .withInstanceCount(taskInstanceCount)
          .withMarket(taskMarketType)
      //build task market type
      if (coreMarketType=="SPOT") coreGroupConfig.withBidPrice(coreBidPrice)
     
      val clusterGroups     = List (masterGroupConfig,coreGroupConfig,taskGroupConfig)
      
      val addInstanceGroupsRequest     = new AddInstanceGroupsRequest()
          addInstanceGroupsRequest.withInstanceGroups(clusterGroups)
          
          
      val amazonElasticMapReduceClient = new AmazonElasticMapReduceClient()
      	  amazonElasticMapReduceClient.addInstanceGroups(addInstanceGroupsRequest)
      	  amazonElasticMapReduceClient

    }
    
    def addJobFlowSteps(amazonElasticMapReduceClient:AmazonElasticMapReduceClient) :AmazonElasticMapReduceClient=
    {
      
      val steps = new StepConfig()
      val addJobFlowStepsRequest = new AddJobFlowStepsRequest()
      .withSteps(steps)
      amazonElasticMapReduceClient.addJobFlowSteps(addJobFlowStepsRequest)
      amazonElasticMapReduceClient
    }
    
  
}

class EMRClient(credentials: Credentials = Credentials.defaultEnv)
  extends aws.AmazonElasticMapReduceClient(credentials)
  with EMR