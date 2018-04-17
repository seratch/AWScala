package awscala.route53

import com.amazonaws.services.route53.model._
import com.amazonaws.services.route53.AmazonRoute53ClientBuilder

trait AmazonRoute53ResourceRecordClientInterface
{
    def listResourceRecordSets(listResourceRecordSetsRequest : ListResourceRecordSetsRequest) : ListResourceRecordSetsResult
    
    def changeResourceRecordSets(changeResourceRecordSetsRequest : ChangeResourceRecordSetsRequest) : ChangeResourceRecordSetsResult
    
    def listHostedZonesByName(listHostedZonesByNameRequest : ListHostedZonesByNameRequest) : ListHostedZonesByNameResult
}

class AmazonRoute53ResourceRecordClient(awsRegion : String) extends AmazonRoute53ResourceRecordClientInterface
{
    private val m_client = AmazonRoute53ClientBuilder.standard().withRegion(awsRegion).build()
    
    def listResourceRecordSets(listResourceRecordSetsRequest : ListResourceRecordSetsRequest) : ListResourceRecordSetsResult =
    {
        m_client.listResourceRecordSets(listResourceRecordSetsRequest)
    }
    
    def changeResourceRecordSets(changeResourceRecordSetsRequest : ChangeResourceRecordSetsRequest) : ChangeResourceRecordSetsResult =
    {
        m_client.changeResourceRecordSets(changeResourceRecordSetsRequest)
    }
    
    def listHostedZonesByName(listHostedZonesByNameRequest : ListHostedZonesByNameRequest) : ListHostedZonesByNameResult =
    {
        m_client.listHostedZonesByName(listHostedZonesByNameRequest)
    }
}