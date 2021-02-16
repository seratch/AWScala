package awscala.dynamodbv2

import DynamoDB.{ SimplePk, CompositePk }
import java.lang.reflect.Modifier
import com.amazonaws.services.{ dynamodbv2 => aws }
import scala.reflect.ClassTag
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

  def batchGet(attributes: List[SimplePk])(implicit dynamoDB: DynamoDB): Seq[Item] =
    batchGetItems(attributes)

  def batchGet(attributes: List[CompositePk])(implicit dynamoDB: DynamoDB, di: DummyImplicit): Seq[Item] =
    batchGetItems(attributes)

  def batchGetItems(attributes: List[SimplePk])(implicit dynamoDB: DynamoDB): Seq[Item] =
    dynamoDB.batchGet[SimplePk](Map(this -> attributes))

  def batchGetItems(attributes: List[CompositePk])(implicit dynamoDB: DynamoDB, di: DummyImplicit): Seq[Item] =
    dynamoDB.batchGet[CompositePk](Map(this -> attributes))

  def put(hashPK: Any, attributes: SimplePk*)(implicit dynamoDB: DynamoDB): Unit = putItem(hashPK, attributes: _*)
  def put(hashPK: Any, rangePK: Any, attributes: SimplePk*)(implicit dynamoDB: DynamoDB): Unit = putItem(hashPK, rangePK, attributes: _*)

  def putItem[T: u.TypeTag](entity:T)(implicit dynamoDB: DynamoDB): Unit = {
    val annotations = getterAnnotationsFromEntity(entity)
    val fields = getterNamesFromEntity(entity)

    val faJoined = annotations.map(tup1 => fields.filter(tup2 => tup2._1 == tup1._1.toString)
      .map(tup2 => (tup1._1.toString, tup1._2.head.toString, {
        tup1._3.toString match {
          case "Int" => tup2._2.asInstanceOf[Int]
          case "String" => tup2._2.asInstanceOf[String]
        }
      }, tup1._3.toString)))
    val faJoinedFlat = faJoined.flatten

    val hashKey = faJoinedFlat.filter(x => x._2.contains("hashPK")).map(y=>y._3).headOption
    val rangeKey = faJoinedFlat.filter(x => x._2.contains("rangePK")).map(y=>y._3).headOption

    val attributes = fields.filter(f => !faJoinedFlat.exists(_._1 == f._1))
    if(hashKey.isEmpty)
      throw new Exception(s"Primary key is not defined for ${entity.getClass.getName}")

    if(hashKey.isDefined && rangeKey.isDefined)
      dynamoDB.put(this, hashKey.get, rangeKey.get, attributes: _*)
    else if(hashKey.isDefined)
      dynamoDB.put(this, hashKey.get, attributes: _*)
  }

  def putItem[E <: AnyRef](hashPK: Any, rangePK: Any, entity: E)(implicit dynamoDB: DynamoDB): Unit = {
    val attrs = getterNamesFromEntity(entity)
    dynamoDB.put(this, hashPK, rangePK, attrs: _*)
  }

  private def getterAnnotationsFromEntity[T: u.TypeTag](entity: T) = {
    u.typeOf[entity.type].decl(termNames.CONSTRUCTOR).asMethod.paramLists.flatten
      .collect({case t: TermSymbol if (t.isVal && t.annotations.nonEmpty) => (t.name,t.annotations,t.typeSignature)})
  }

  private def getterNamesFromEntity(obj: Any):List[(String, AnyRef)] = {
    val fieldNames = obj.getClass.getDeclaredFields
      .filter(f => Modifier.isPrivate(f.getModifiers))
      .filterNot(f => Modifier.isStatic(f.getModifiers))
      .map(_.getName)

    val methodNames = obj.getClass.getDeclaredMethods
      .filter(m => Modifier.isPublic(m.getModifiers))
      .filterNot(m => Modifier.isStatic(m.getModifiers))
      .filterNot(m => m.getParameterTypes.length > 0)
      .map(_.getName)

    methodNames.filter(m => fieldNames.contains(m))
    .map(getterName => {
      val value = obj.getClass.getDeclaredMethod(getterName).invoke(obj)
      getterName -> value
    }).toList
  }
  
  def putItem(hashPK: Any, attributes: SimplePk*)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.put(this, hashPK, attributes: _*)
  }
  def putItem(hashPK: Any, rangePK: Any, attributes: SimplePk*)(implicit dynamoDB: DynamoDB): Unit = {
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

  def addAttributes(hashPK: Any, attributes: SimplePk*)(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, None, aws.model.AttributeAction.ADD, attributes)
  }
  def addAttributes(hashPK: Any, rangePK: Any, attributes: Seq[SimplePk])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, Some(rangePK), aws.model.AttributeAction.ADD, attributes)
  }

  def deleteAttributes(hashPK: Any, attributes: Seq[SimplePk])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, None, aws.model.AttributeAction.DELETE, attributes)
  }
  def deleteAttributes(hashPK: Any, rangePK: Any, attributes: Seq[SimplePk])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, Some(rangePK), aws.model.AttributeAction.DELETE, attributes)
  }

  def putAttributes(hashPK: Any, attributes: Seq[SimplePk])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, None, aws.model.AttributeAction.PUT, attributes)
  }
  def putAttributes(hashPK: Any, rangePK: Any, attributes: Seq[SimplePk])(
    implicit
    dynamoDB: DynamoDB): Unit = {
    dynamoDB.updateAttributes(this, hashPK, Some(rangePK), aws.model.AttributeAction.PUT, attributes)
  }

  def update(throughput: ProvisionedThroughput)(implicit dynamoDB: DynamoDB): TableMeta = {
    dynamoDB.updateTableProvisionedThroughput(this, throughput)
  }

  def destroy()(implicit dynamoDB: DynamoDB): Unit = dynamoDB.delete(this)

}
