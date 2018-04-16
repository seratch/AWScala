package awscala.firehose

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesisfirehose.model.{PutRecordRequest, PutRecordBatchRequest, PutRecordResult, PutRecordBatchResult, Record}
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsyncClient
import com.amazonaws.services.{ kinesisfirehose => aws }
import awscala._
import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext


object KinesisFirehose 
{    
    def apply(credentials : Credentials)(implicit region: Region) : KinesisFirehose  =
    {
        new KinesisFirehoseAsyncClient(BasicCredentialsProvider(credentials.getAWSAccessKeyId, credentials.getAWSSecretKey)).withRegion(region)
    }
    
    def apply(credentialsProvider : CredentialsProvider = CredentialsLoader.load())(implicit region : Region) : KinesisFirehose  =
    {
        new KinesisFirehoseAsyncClient(credentialsProvider).withRegion(region)
    }
    
    def apply(accessKeyId : String, secretAccessKey: String)(implicit region : Region) : KinesisFirehose =
    {
        new KinesisFirehoseAsyncClient(BasicCredentialsProvider(accessKeyId, secretAccessKey)).withRegion(region)
    }
}

trait KinesisFirehose extends aws.AmazonKinesisFirehoseAsyncClient
{
    
    def convertToScalaFuture[T](javaFuture : java.util.concurrent.Future[T])(implicit executor : ExecutionContext) : Future[T] =
    {
        Future 
        {
            javaFuture.get()
        }
    }
    
    def sendBulkMessagesToFirehoseAsync(payloads : Seq[String], deliveryStream : String)(implicit executor : ExecutionContext) : Future[PutRecordBatchResult] = 
    {
        val putRecordBatchRequest = createRecordBatchRequest(payloads, deliveryStream)
        val javaFuture = putRecordBatchAsync(putRecordBatchRequest)
        convertToScalaFuture(javaFuture)
    }
    
    def sendBulkMessagesToFirehose(payloads : Seq[String], deliveryStream : String) : PutRecordBatchResult = 
    {
        val putRecordBatchRequest = createRecordBatchRequest(payloads, deliveryStream)
        putRecordBatch(putRecordBatchRequest)
    }
    
    def sendMessageToFirehoseAsync(payload : String, deliveryStream : String)(implicit executor : ExecutionContext) : Future[PutRecordResult] = 
    {
        val putRecordRequest = createRecordRequest(payload, deliveryStream)
        val javaFuture = putRecordAsync(putRecordRequest)
        convertToScalaFuture(javaFuture)
    }
    
    def sendMessageToFirehose(payload : String, deliveryStream : String) : PutRecordResult =
    {
        val putRecordRequest = createRecordRequest(payload, deliveryStream)
        putRecord(putRecordRequest)
    }
    
    def createRecordBatchRequest(payloads : Seq[String], deliveryStream : String) : PutRecordBatchRequest =
    {
        val records = payloads.map(
            payload =>
            {
                createRecord(payload)
            }).asJava
        new PutRecordBatchRequest().withDeliveryStreamName(deliveryStream).withRecords(records)
    }
    
    def createRecordRequest(payload : String, deliveryStream : String) : PutRecordRequest =
    {
        val deliveryStreamRecord = createRecord(payload)
        new PutRecordRequest().withDeliveryStreamName(deliveryStream).withRecord(deliveryStreamRecord)
    }
        
    def createRecord(payload : String) : Record =
    {
        val data = ByteBuffer.wrap(payload.getBytes(StandardCharsets.UTF_8))
        new Record().withData(data)
    }   
}

class KinesisFirehoseAsyncClient(credentialsProvider : CredentialsProvider = CredentialsLoader.load())
  extends aws.AmazonKinesisFirehoseAsyncClient(credentialsProvider)
  with KinesisFirehose
