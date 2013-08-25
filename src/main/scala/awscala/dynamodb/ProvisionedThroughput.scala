package awscala.dynamodb

import com.amazonaws.services.{ dynamodbv2 => aws }

object ProvisionedThroughput {
  def apply(meta: ProvisionedThroughputMeta): ProvisionedThroughput = new ProvisionedThroughput(
    readCapacityUnits = meta.readCapacityUnits,
    writeCapacityUnits = meta.writeCapacityUnits
  )
}

case class ProvisionedThroughput(
    readCapacityUnits: Long,
    writeCapacityUnits: Long) extends aws.model.ProvisionedThroughput {

  setReadCapacityUnits(readCapacityUnits)
  setWriteCapacityUnits(writeCapacityUnits)
}
