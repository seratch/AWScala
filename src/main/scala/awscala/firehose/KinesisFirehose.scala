package awscala.firehose


import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesisfirehose.model.{PutRecordRequest, PutRecordResult, Record}
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsyncClient
import com.amazonaws.services.{ kinesisfirehose => aws }
import awscala._
import scala.collection.JavaConverters._



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
    
    def sendAsyncMessageToFirehose(payload : String, deliveryStream : String) : java.util.concurrent.Future[PutRecordResult] = 
    {
        val putRecordRequest = createRecordRequest(payload, deliveryStream)
        this.putRecordAsync(putRecordRequest)
    }
    
    def sendMessageToFirehose(payload : String, deliveryStream : String) : PutRecordResult =
    {
        val putRecordRequest = createRecordRequest(payload, deliveryStream)
        this.putRecord(putRecordRequest)
    }
    
    def createRecordRequest(payload : String, deliveryStream : String) : PutRecordRequest =
    {
        val data = ByteBuffer.wrap(payload.getBytes(StandardCharsets.UTF_8))
        val deliveryStreamRecord : Record = new Record().withData(data)
        new PutRecordRequest().withDeliveryStreamName(deliveryStream).withRecord(deliveryStreamRecord)
    }
}

class KinesisFirehoseAsyncClient(credentialsProvider : CredentialsProvider = CredentialsLoader.load())
  extends aws.AmazonKinesisFirehoseAsyncClient(credentialsProvider)
  with KinesisFirehose
