package awscala.dynamodbv2

import DynamoDB.SimplePk

private[dynamodbv2] trait TableCompat { self: Table =>
  def putItem(hashPK: Any, attributes: SimplePk*)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.put(this, hashPK, attributes: _*)
  }
  def putItem(hashPK: Any, rangePK: Any, attributes: SimplePk*)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.put(this, hashPK, rangePK, attributes: _*)
  }
}