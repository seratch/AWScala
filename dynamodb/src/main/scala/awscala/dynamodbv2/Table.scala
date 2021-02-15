package awscala.dynamodbv2

import java.lang.reflect.Modifier
import com.amazonaws.services.{dynamodbv2 => aws}
import scala.annotation.StaticAnnotation
import scala.reflect.runtime.{universe => u}
import scala.reflect.runtime.universe.{TermSymbol, runtimeMirror, termNames}

object Table {
  def apply(
    name: String,
    hashPK: String,
    rangePK: Option[String],
    attributes: Seq[AttributeDefinition],
    localSecondaryIndexes: Seq[LocalSecondaryIndex],
    globalSecondaryIndexes: Seq[GlobalSecondaryIndex],
    provisionedThroughput: Option[ProvisionedThroughput],
    billingMode: aws.model.BillingMode): Table =
    new Table(name, hashPK, rangePK, attributes, localSecondaryIndexes,
      globalSecondaryIndexes, provisionedThroughput, Option(billingMode))
}

class hashPK extends StaticAnnotation
class rangePK extends StaticAnnotation

case class Table(
  name: String,
  hashPK: String,
  rangePK: Option[String] = None,
  attributes: Seq[AttributeDefinition] = Nil,
  localSecondaryIndexes: Seq[LocalSecondaryIndex] = Nil,
  globalSecondaryIndexes: Seq[GlobalSecondaryIndex] = Nil,
  provisionedThroughput: Option[ProvisionedThroughput] = None,
  billingMode: Option[aws.model.BillingMode] = None) {

  // ------------------------------------------
  // Items
  // ------------------------------------------

  def get(hashPK: Any)(implicit dynamoDB: DynamoDB): Option[Item] = getItem(hashPK)
  def get(hashPK: Any, rangePK: Any)(implicit dynamoDB: DynamoDB): Option[Item] = getItem(hashPK, rangePK)

  def getItem(hashPK: Any)(implicit dynamoDB: DynamoDB): Option[Item] = {
    dynamoDB.get(this, hashPK)
  }
  def getItem(hashPK: Any, rangePK: Any)(
    implicit
    dynamoDB: DynamoDB): Option[Item] = {
    dynamoDB.get(this, hashPK, rangePK)
  }

  def batchGet(attributes: List[(String, Any)])(implicit dynamoDB: DynamoDB): Seq[Item] = batchGetItems(attributes)

  def batchGetItems(attributes: List[(String, Any)])(implicit dynamoDB: DynamoDB): Seq[Item] = {
    dynamoDB.batchGet(Map(this -> attributes))
  }

  def put(hashPK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB): Unit = putItem(hashPK, attributes: _*)
  def put(hashPK: Any, rangePK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB): Unit = putItem(hashPK, rangePK, attributes: _*)

//  @deprecated
//  def putItem[E <: AnyRef](entity: E)(implicit dynamoDB: DynamoDB): Unit = {
//    val attrs = getAttrValuesToMap(entity, true)
//
//    if (attrs("keys").exists(x => x._1 == "hashPK") && attrs("keys").exists(x => x._1 == "rangePK")) {
//      val hashPK = attrs("keys").find(f => f._1 == "hashPK").get._2
//      val rangePK = attrs("keys").find(f => f._1 == "rangePK").get._2
//      dynamoDB.put(this, hashPK, rangePK, attrs("attributes"): _*)
//    } else if (attrs("keys").exists(x => x._1 == "hashPK")) {
//      val hashPK = attrs("keys").find(f => f._1 == "hashPK").get._2
//      dynamoDB.put(this, hashPK, attrs("attributes"): _*)
//    }
//  }

  def putItem[T: u.TypeTag](entity:T)(implicit dynamoDB: DynamoDB): Unit = {
    val annotations = getterAnnotationsFromEntity(entity)
    val fields = getterNamesFromEntity(entity).map(getterName => {
      val value = entity.getClass.getDeclaredMethod(getterName).invoke(entity)
      getterName -> value
    }).toList

    val faJoined = annotations.map(tup1 => fields.filter(tup2 => tup2._1 == tup1._1.toString)
      .map(tup2 => (tup1._1.toString, tup1._2.head.toString, {
        tup1._3.toString match {
          case "Int" => tup2._2.asInstanceOf[Int]
          case "String" => tup2._2.asInstanceOf[String]
        }
      }, tup1._3.toString)))
    val faJoinedFlat = faJoined.flatten

    val hashKey = faJoinedFlat.filter(x => x._2.contains("hashPK")).map(y=>y._3).head
    val rangeKey = faJoinedFlat.filter(x => x._2.contains("rangePK")).map(y=>y._3).head

    val attributes = fields.filter(f => !faJoinedFlat.exists(_._1 == f._1))
    dynamoDB.put(this, hashKey, rangeKey, attributes: _*)
  }

  def putItem[E <: AnyRef](hashPK: Any, rangePK: Any, entity: E)(implicit dynamoDB: DynamoDB): Unit = {
    val attrs = getAttrValuesToMap(entity, false)
    dynamoDB.put(this, hashPK, rangePK, attrs("attributes"): _*)
  }

  private def getAttrValuesToMap(entity: Any, keysRequired: Boolean): Map[String, List[(String, AnyRef)]] = {
    val fields = getterNamesFromEntity(entity).map(getterName => {
      val value = entity.getClass.getDeclaredMethod(getterName).invoke(entity)
      getterName -> value
    }).toList

    val keys = fields.filter(f => f._1 == "hashPK" || f._1 == "rangePK")
    val attrs = fields.filterNot(f => keys.exists(k => f._1 == k._1))

    if (!keys.exists(k => k._1 == "hashPK") && keysRequired)
      throw new Exception(s"Primary key is not defined for ${entity.getClass.getName}")

    Map("keys" -> keys, "attributes" -> attrs)
  }

  private def getterAnnotationsFromEntity[T: u.TypeTag](entity: T) = {
    u.typeOf[entity.type].decl(termNames.CONSTRUCTOR).asMethod.paramLists.flatten
      .collect({case t: TermSymbol if (t.isVal && t.annotations.nonEmpty) => (t.name,t.annotations,t.typeSignature)})
  }

  private def getterNamesFromEntity(obj: Any): Seq[String] = {
    val fieldNames = obj.getClass.getDeclaredFields
      .filter(f => Modifier.isPrivate(f.getModifiers))
      .filterNot(f => Modifier.isStatic(f.getModifiers))
      .map(_.getName)

    val methodNames = obj.getClass.getDeclaredMethods
      .filter(m => Modifier.isPublic(m.getModifiers))
      .filterNot(m => Modifier.isStatic(m.getModifiers))
      .filterNot(m => m.getParameterTypes.size > 0)
      .map(_.getName)

    methodNames.filter(m => fieldNames.contains(m))
  }



  def putItem(hashPK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.put(this, hashPK, attributes: _*)
  }
  def putItem(hashPK: Any, rangePK: Any, attributes: (String, Any)*)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.put(this, hashPK, rangePK, attributes: _*)
  }

  def delete(hashPK: Any)(implicit dynamoDB: DynamoDB): Unit = deleteItem(hashPK)
  def delete(hashPK: Any, rangePK: Any)(implicit dynamoDB: DynamoDB): Unit = deleteItem(hashPK, rangePK)

  def deleteItem(hashPK: Any)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.deleteItem(this, hashPK)
  }
  def deleteItem(hashPK: Any, rangePK: Any)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.deleteItem(this, hashPK, rangePK)
  }

  def queryWithIndex(
    index: SecondaryIndex,
    keyConditions: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    scanIndexForward: Boolean = true,
    consistentRead: Boolean = false,
    limit: Int = 1000,
    pageStatsCallback: PageStats => Unit = null)(implicit dynamoDB: DynamoDB): Seq[Item] = {
    dynamoDB.queryWithIndex(
      table = this,
      index = index,
      keyConditions = keyConditions,
      select = select,
      attributesToGet = attributesToGet,
      scanIndexForward = scanIndexForward,
      consistentRead = consistentRead,
      limit = limit,
      pageStatsCallback = pageStatsCallback)
  }

  def query(
    keyConditions: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    scanIndexForward: Boolean = true,
    consistentRead: Boolean = false,
    limit: Int = 1000,
    pageStatsCallback: PageStats => Unit = null)(implicit dynamoDB: DynamoDB): Seq[Item] = {
    dynamoDB.query(
      table = this,
      keyConditions = keyConditions,
      select = select,
      attributesToGet = attributesToGet,
      scanIndexForward = scanIndexForward,
      consistentRead = consistentRead,
      limit = limit,
      pageStatsCallback = pageStatsCallback)
  }

  def scan(
    filter: Seq[(String, aws.model.Condition)],
    select: Select = aws.model.Select.ALL_ATTRIBUTES,
    attributesToGet: Seq[String] = Nil,
    limit: Int = 1000,
    segment: Int = 0,
    totalSegments: Int = 1,
    consistentRead: Boolean = false,
    pageStatsCallback: PageStats => Unit = null)(implicit dynamoDB: DynamoDB): Seq[Item] = {
    dynamoDB.scan(
      table = this,
      filter = filter,
      limit = limit,
      segment = segment,
      totalSegments = totalSegments,
      select = select,
      attributesToGet = attributesToGet,
      consistentRead = consistentRead,
      pageStatsCallback = pageStatsCallback)
  }

  def addAttributes(hashPK: Any, attributes: (String, Any)*)(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, None, aws.model.AttributeAction.ADD, attributes)
  }
  def addAttributes(hashPK: Any, rangePK: Any, attributes: Seq[(String, Any)])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, Some(rangePK), aws.model.AttributeAction.ADD, attributes)
  }

  def deleteAttributes(hashPK: Any, attributes: Seq[(String, Any)])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, None, aws.model.AttributeAction.DELETE, attributes)
  }
  def deleteAttributes(hashPK: Any, rangePK: Any, attributes: Seq[(String, Any)])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, Some(rangePK), aws.model.AttributeAction.DELETE, attributes)
  }

  def putAttributes(hashPK: Any, attributes: Seq[(String, Any)])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, None, aws.model.AttributeAction.PUT, attributes)
  }
  def putAttributes(hashPK: Any, rangePK: Any, attributes: Seq[(String, Any)])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, Some(rangePK), aws.model.AttributeAction.PUT, attributes)
  }

  def update(throughput: ProvisionedThroughput)(implicit dynamoDB: DynamoDB): TableMeta = {
    dynamoDB.updateTableProvisionedThroughput(this, throughput)
  }

  def destroy()(implicit dynamoDB: DynamoDB): Unit = dynamoDB.delete(this)

}
