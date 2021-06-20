package awscala.dynamodbv2

import DynamoDB.SimplePk

import scala.reflect.runtime.{ universe => u }
import scala.reflect.runtime.universe.termNames

import scala.collection.mutable.ListBuffer
import scala.annotation.StaticAnnotation

class hashPK extends StaticAnnotation
class rangePK extends StaticAnnotation

private[dynamodbv2] trait TableCompat { self: Table =>

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
        throw new Exception(s"Primary key is not defined for ${entity.getClass.getName} (constructor args are $constructorArgs)")
    }
  }

  def putItem(hashPK: Any, attributes: SimplePk*)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.put(this, hashPK, attributes: _*)
  }
  def putItem(hashPK: Any, rangePK: Any, attributes: SimplePk*)(implicit dynamoDB: DynamoDB): Unit = {
    dynamoDB.put(this, hashPK, rangePK, attributes: _*)
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
}
