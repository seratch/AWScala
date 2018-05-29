package awscala.dynamodbv2

import com.amazonaws.services.{ dynamodbv2 => aws }

case class PageStats(page: Int, lastPage: Boolean, limit: Int, scanned: Int, items: Int, consumedCapacity: aws.model.ConsumedCapacity)
