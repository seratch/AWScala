package awscala.redshift

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ redshift => aws }

object ReservedNode {

  def apply(n: aws.model.ReservedNode): ReservedNode = new ReservedNode(
    id = n.getReservedNodeId,
    state = n.getState,
    currencyCode = n.getCurrencyCode,
    duration = n.getDuration,
    fixedPrice = n.getFixedPrice,
    usagePrice = n.getUsagePrice,
    nodeCount = n.getNodeCount,
    nodeType = NodeType(n.getNodeType),
    offeringId = n.getReservedNodeOfferingId,
    offeringType = n.getOfferingType,
    recurringCharges = n.getRecurringCharges.asScala.map(c => RecurringCharge(c)).toSeq,
    startedAt = new DateTime(n.getStartTime)
  )
}

class ReservedNode(
  id: String,
  state: String,
  currencyCode: String,
  duration: Int,
  fixedPrice: Double,
  usagePrice: Double,
  nodeCount: Int,
  nodeType: NodeType,
  offeringId: String,
  offeringType: String,
  recurringCharges: Seq[RecurringCharge],
  startedAt: DateTime)
  extends aws.model.ReservedNode {

  setCurrencyCode(currencyCode)
  setDuration(duration)
  setFixedPrice(fixedPrice)
  setNodeCount(nodeCount)
  setNodeType(nodeType.value)
  setOfferingType(offeringType)
  setRecurringCharges(recurringCharges.map(_.asInstanceOf[aws.model.RecurringCharge]).asJava)
  setReservedNodeId(id)
  setReservedNodeOfferingId(offeringId)
  setStartTime(startedAt.toDate)
  setState(state)
  setUsagePrice(usagePrice)
}

case class RecurringCharge(amount: Double, frequency: String) extends aws.model.RecurringCharge {

  setRecurringChargeAmount(amount)
  setRecurringChargeFrequency(frequency)
}