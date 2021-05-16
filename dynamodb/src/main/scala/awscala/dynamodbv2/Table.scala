package awscala.dynamodbv2

import DynamoDB.{ CompositePk, SimplePk }

import java.lang.reflect.Modifier
import com.amazonaws.services.{ dynamodbv2 => aws }

import scala.annotation.StaticAnnotation
import scala.collection.mutable.ListBuffer
import scala.reflect.runtime.{ universe => u }
import scala.reflect.runtime.universe.termNames

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

  def putItem[E <: AnyRef](hashPK: Any, rangePK: Any, entity: E)(implicit dynamoDB: DynamoDB): Unit = {
    val attrs = extractGetterNameAndValue(entity)
    dynamoDB.put(this, hashPK, rangePK, attrs: _*)
  }

  def putItem[T: u.TypeTag](entity: T)(implicit dynamoDB: DynamoDB): Unit = {
    val constructorArgs: Seq[AnnotatedConstructorArgMeta] = extractAnnotatedConstructorArgs(entity)
    val getterCallResults: Seq[(String, AnyRef)] = extractGetterNameAndValue(entity)

    var maybeHashPK: Option[Any] = None
    var maybeRangePK: Option[Any] = None
    val attributes: ListBuffer[(String, AnyRef)] = ListBuffer()
    for (nameAndValue <- getterCallResults) {
      val (name, value) = nameAndValue
      constructorArgs.find { arg => name == arg.name } match {
        case Some(arg) if arg.annotationNames.exists(_.contains("hashPK")) => maybeHashPK = Some(value)
        case Some(arg) if arg.annotationNames.exists(_.contains("rangePK")) => maybeRangePK = Some(value)
        case _ => attributes += nameAndValue
      }
    }
    (maybeHashPK, maybeRangePK) match {
      case (Some(hashPK), Some(rangePK)) =>
        dynamoDB.put(this, hashPK, rangePK, attributes.toSeq: _*)
      case (Some(hashPK), None) =>
        dynamoDB.put(this, hashPK, attributes.toSeq: _*)
      case _ =>
        throw new Exception(s"Primary key is not defined for ${entity.getClass.getName}")
    }
  }

  private case class AnnotatedConstructorArgMeta(name: String, annotationNames: Seq[String])

  private def extractAnnotatedConstructorArgs[T: u.TypeTag](entity: T): Seq[AnnotatedConstructorArgMeta] = {
    u.typeOf[entity.type].decl(termNames.CONSTRUCTOR).asMethod.paramLists.flatten
      .collect({
        case t if t != null && t.annotations.nonEmpty =>
          Some(AnnotatedConstructorArgMeta(
            name = t.name.toString,
            // FIXME: should we use canonical name?
            annotationNames = t.annotations.map(_.toString)))
        case _ => None
      }).flatten
  }

  private def extractGetterNameAndValue(obj: Any): Seq[(String, AnyRef)] = {
    val clazz = obj.getClass
    val privateInstanceFieldNames: Seq[String] = clazz.getDeclaredFields
      .filter(f =>
        Modifier.isPrivate(f.getModifiers)
          && !Modifier.isStatic(f.getModifiers))
      .toIndexedSeq // To avoid implicit conversion
      .map(_.getName)
    val getterMethodNames: Seq[String] = clazz.getDeclaredMethods
      .filter(m =>
        Modifier.isPublic(m.getModifiers)
          && !Modifier.isStatic(m.getModifiers)
          && m.getParameterTypes.length == 0
          && privateInstanceFieldNames.contains(m.getName))
      .toIndexedSeq // To avoid implicit conversion
      .map(_.getName)

    getterMethodNames.map(name => (name, clazz.getDeclaredMethod(name).invoke(obj)))
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
