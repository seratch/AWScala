package awscala.route53

import com.amazonaws.services.route53.AmazonRoute53AsyncClientBuilder
import com.amazonaws.services.route53.model.ChangeBatch
import com.amazonaws.services.route53.model.Change
import com.amazonaws.services.route53.model.ResourceRecordSet
import com.amazonaws.services.route53.model.ResourceRecord
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.util.Try
import scala.util.Success
import com.amazonaws.services.route53.model.ChangeAction
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder
import java.net.URI
import scala.util.Failure
import java.net.URL
import com.google.common.net.InternetDomainName
import com.amazonaws.services.route53.model.ListHostedZonesByNameResult
import com.amazonaws.services.route53.model._

class Route53Exception(message : String) extends Exception(message)

object Route53
{
    val TTL = 0L
    val UPSERT_CHANGE_ACTION_STRING = ChangeAction.UPSERT.toString
    val IPV4_RESOURCE_RECORD_TYPE = "A"
    val HOSTED_ZONE_PREFIX = "/hostedzone/"
    val EMPTY_STRING = ""
    
    def getResourceRecordSetValues(m_client : AmazonRoute53ResourceRecordClientInterface, hostedZoneId : String, recordNameWithoutFormating : String)(implicit ec : ExecutionContext) : Future[Seq[String]] =
    {
        val recordName = recordSetNameFormatting(recordNameWithoutFormating)
        val resourcesRecordSetsFuture = getResourcesRecordSets(m_client, hostedZoneId, recordName)
        resourcesRecordSetsFuture.map
        {
            listResourceRecordSet =>
            {
                val resourceRecordSetOption = listResourceRecordSet.getResourceRecordSets.asScala.headOption
                
                resourceRecordSetOption match
                {
                    case Some(resourceRecordSet) if resourceRecordSet.getName == recordName =>
                    {
                        val resourceRecords = resourceRecordSet.getResourceRecords.asScala
                        resourceRecords.map(_.getValue)
                    }
                    
                    case _ =>
                    {
                        throw new Route53Exception(s"Can't found in Route53 Record name with name : $recordName  in hosted zone : $hostedZoneId resourceRecordSetOption : $resourceRecordSetOption")
                    }
                }
            }
        }
    }
    
    private def getResourcesRecordSets(m_client : AmazonRoute53ResourceRecordClientInterface, hostedZoneId : String, recordName : String)(implicit ec : ExecutionContext) : Future[ListResourceRecordSetsResult] =
    {
        val listResourceRecordSetsRequest = new ListResourceRecordSetsRequest().withHostedZoneId(hostedZoneId).withStartRecordName(recordName) 
        
        Future
        {
            val listResourceRecordSetsResult = m_client.listResourceRecordSets(listResourceRecordSetsRequest)
            listResourceRecordSetsResult
        }
    }
    
    def changeResourceRecordSet(m_client : AmazonRoute53ResourceRecordClientInterface, hostedZoneId : String, recordName : String, newValue : String)(implicit ec : ExecutionContext) : Future[ChangeResourceRecordSetsResult] =
    {
        val changeResourceRecordSetsRequest = createChangeResourceRecordSetRequest(hostedZoneId, recordName, newValue)
        
        Future
        {
            val changeResourceRecordSetsResult = m_client.changeResourceRecordSets(changeResourceRecordSetsRequest)
            changeResourceRecordSetsResult
        }
    }
    
    private def createChangeResourceRecordSetRequest(hostedZoneId : String, recordName : String, resourceRecordValue : String) : ChangeResourceRecordSetsRequest =
    {
        val resourceRecord = new ResourceRecord().withValue(resourceRecordValue)
        val resourceRecordSet = new ResourceRecordSet().withName(recordName).withTTL(TTL).withResourceRecords(resourceRecord).withType(IPV4_RESOURCE_RECORD_TYPE)
        val changes = new Change().withAction(UPSERT_CHANGE_ACTION_STRING).withResourceRecordSet(resourceRecordSet)
        val changeBatch = new ChangeBatch().withChanges(changes)
        val request = new ChangeResourceRecordSetsRequest().withHostedZoneId(hostedZoneId).withChangeBatch(changeBatch)
                            
        request
    }
    
    def getHostedZoneIDByAddress(m_client : AmazonRoute53ResourceRecordClientInterface, address : String)(implicit ec : ExecutionContext) : Future[String] =
    {
        val dnsNameTry = Try
        {
            InternetDomainName.from(address).topPrivateDomain.toString
        }
        
        dnsNameTry match
        {
            case Success(dnsName) =>
            {
                val listHostedZonesByNameRequest = new ListHostedZonesByNameRequest().withDNSName(dnsName)

                Future
                {
                    val listHostedZonesByNameResult = m_client.listHostedZonesByName(listHostedZonesByNameRequest)
                    val hostedZoneOption = listHostedZonesByNameResult.getHostedZones.asScala.headOption
                    
                    hostedZoneOption match
                    {
                        case Some(hostedZone) if hostedZone.getName == recordSetNameFormatting(dnsName)  =>
                        {
                            hostedZone.getId.replace(HOSTED_ZONE_PREFIX, EMPTY_STRING)
                        }
                        
                        case _ =>
                        {
                            throw new Route53Exception(s"Could not get hosted zone id for dns name: $dnsName, get hosted zone: $hostedZoneOption") 
                        }
                    }
                }
            }
            
            case Failure(cause) =>
            {
                throw new Route53Exception(s"Could not get hosted zone id for address: $address cause: $cause") 
            }
        }
    }
    
    private def recordSetNameFormatting(recordSetName : String) : String =
    {
        s"$recordSetName."
    }
}
