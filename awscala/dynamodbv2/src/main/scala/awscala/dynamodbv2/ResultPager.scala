package awscala.dynamodbv2

import com.amazonaws.services.{ dynamodbv2 => aws }
import scala.collection.JavaConverters._

/**
 * The ResultPager allows iteration over the results from a DynamoDB query/scan as a single stream of items,
 * handling the necessary paging details in the background.
 *
 * DynamoDB paginates the result of query/scan operations. The data returned from a Query or Scan operation is limited
 * to 1 MB; this means that if the result set exceeds 1 MB of data, you'll need to perform another Query or Scan
 * operation to retrieve the next 1 MB of data. In addition, the limit parameter controls the number of items that you
 * want DynamoDB to process in a single request before returning results.
 * See http://docs.aws.amazon.com/amazondynamodb/latest/developerguide/QueryAndScan.html#Pagination
 *
 * Each response from DynamoDB indicates if the processing has reached the end of the dataset, or if another request is
 * needed in order to continue scanning where the last request finished.
 *
 * When the items from a page is exhausted, the ResultPager will issue a new query/scan for the next page of results,
 * until processing reaches the end of the dataset, or the client stops iterating over the result (as the return value
 * is a Stream[Item])
 *
 * @tparam TReq
 * @tparam TRes
 */
sealed trait ResultPager[TReq, TRes] extends Iterator[Item] {
  def table: Table
  def operation: TReq => TRes
  def request: TReq

  var items: Seq[Item] = null
  var pageNo = 0
  var lastKey: java.util.Map[String, aws.model.AttributeValue] = null
  var index = 0
  nextPage(request)

  def getItems(result: TRes): java.util.List[java.util.Map[String, aws.model.AttributeValue]]
  def invokeCallback(result: TRes): Unit
  def getLastEvaluatedKey(result: TRes): java.util.Map[String, aws.model.AttributeValue]
  def withExclusiveStartKey(request: TReq, lastKey: java.util.Map[String, aws.model.AttributeValue]): TReq

  private def nextPage(request: TReq): Unit = {
    val result = operation(request)
    invokeCallback(result)
    items = getItems(result).asScala.map(i => Item(table, i))
    lastKey = getLastEvaluatedKey(result)
    index = 0
    pageNo += 1
  }

  override def next(): Item = {
    val item: Item = items(index)
    index += 1
    item
  }

  override def hasNext: Boolean = {
    if (index < items.size) {
      true
    } else if (lastKey == null) {
      false
    } else {
      do {
        nextPage(withExclusiveStartKey(request, lastKey))
      } while (lastKey != null && items.size == 0) // there are potentially more matching data, but this page didn't contain any
      items.size != 0
    }
  }
}

// a pager specialized for query request/results
class QueryResultPager(val table: Table, val operation: aws.model.QueryRequest => aws.model.QueryResult, val request: aws.model.QueryRequest, pageStatsCallback: PageStats => Unit)
    extends ResultPager[aws.model.QueryRequest, aws.model.QueryResult] {
  override def getItems(result: aws.model.QueryResult) = result.getItems
  override def getLastEvaluatedKey(result: aws.model.QueryResult) = result.getLastEvaluatedKey
  override def withExclusiveStartKey(request: aws.model.QueryRequest, lastKey: java.util.Map[String, aws.model.AttributeValue]) = request.withExclusiveStartKey(lastKey)
  override def invokeCallback(result: aws.model.QueryResult) = {
    Option(pageStatsCallback).map(fun => fun(PageStats(pageNo, result.getLastEvaluatedKey == null, request.getLimit, result.getScannedCount, result.getCount, result.getConsumedCapacity)))
  }
}

// a pager specialized for scan request/results
class ScanResultPager(val table: Table, val operation: aws.model.ScanRequest => aws.model.ScanResult, val request: aws.model.ScanRequest, pageStatsCallback: PageStats => Unit)
    extends ResultPager[aws.model.ScanRequest, aws.model.ScanResult] {
  override def getItems(result: aws.model.ScanResult) = result.getItems
  override def getLastEvaluatedKey(result: aws.model.ScanResult) = result.getLastEvaluatedKey
  override def withExclusiveStartKey(request: aws.model.ScanRequest, lastKey: java.util.Map[String, aws.model.AttributeValue]) = request.withExclusiveStartKey(lastKey)
  override def invokeCallback(result: aws.model.ScanResult) = {
    Option(pageStatsCallback).map(fun => fun(PageStats(pageNo, result.getLastEvaluatedKey == null, request.getLimit, result.getScannedCount, result.getCount, result.getConsumedCapacity)))
  }
}
