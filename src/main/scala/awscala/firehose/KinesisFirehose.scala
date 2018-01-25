package awscala.firehose


import java.util.Date
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import com.amazonaws.regions.{Region, Regions}
import com.amazonaws.services.kinesisfirehose.model.{PutRecordRequest, PutRecordResult, Record}
import com.amazonaws.services.kinesisfirehose.AmazonKinesisFirehoseAsyncClient
import com.amazonaws.services.{ kinesisfirehose => aws }
import awscala._
import scala.collection.JavaConverters._


case class UserMessage(userId: Int, userName: String, timestamp: Date = new Date)

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
    
    def sendMessageToFirehose(payload : String, deliveryStream : String): Unit = 
    {
        val data = ByteBuffer.wrap(payload.getBytes(StandardCharsets.UTF_8))
        val deliveryStreamRecord : Record = new Record().withData(data)
        val putRecordRequest : PutRecordRequest = new PutRecordRequest()
            .withDeliveryStreamName(deliveryStream)
            .withRecord(deliveryStreamRecord)        
        
        this.putRecordAsync(putRecordRequest)
    }
}

class KinesisFirehoseAsyncClient(credentialsProvider : CredentialsProvider = CredentialsLoader.load())
  extends aws.AmazonKinesisFirehoseAsyncClient(credentialsProvider)
  with KinesisFirehose
