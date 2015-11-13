package awscala.dynamodbv2

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }
import java.util.{ Map => JMap }

object AttributeValue {

  private def recurseMapValue(valueMap: Map[String, Any]): Map[String, aws.model.AttributeValue] = valueMap.map {
    case (key, xs: Seq[_]) =>
      key -> toJavaValue(xs)
    case (key, vl: Map[String, Any]) =>
      key -> new aws.model.AttributeValue().withM(recurseMapValue(vl).asJava)
    case (key: String, vl: Object) =>
      key -> toJavaValue(vl)
  }

  def toJavaValue(v: Any): aws.model.AttributeValue = {
    val value = new aws.model.AttributeValue
    v match {
      case null => null
      case s: String => value.withS(s)
      case n: java.lang.Number => value.withN(n.toString)
      case b: ByteBuffer => value.withB(b)
      case xs: Seq[_] => xs.headOption match {
        case Some(m: Map[String, Any]) => value.withL(xs.map(toJavaValue).asJavaCollection)
        case Some(s: String) => value.withSS(xs.map(_.asInstanceOf[String]).asJava)
        case Some(n: java.lang.Number) => value.withSS(xs.map(_.toString).asJava)
        case Some(s: ByteBuffer) => value.withBS(xs.map(_.asInstanceOf[ByteBuffer]).asJava)
        case Some(v) => value.withSS(xs.map(_.toString).asJava)
        case _ => null
      }
      case m: Map[String, Any] => value.withM(recurseMapValue(m).asJava)
      case _ => null
    }
  }

  def apply(v: aws.model.AttributeValue): AttributeValue = new AttributeValue(
    s = Option(v.getS),
    n = Option(v.getN),
    b = Option(v.getB),
    m = Option(v.getM),
    l = Option(v.getL).map(_.asScala).getOrElse(Nil),
    ss = Option(v.getSS).map(_.asScala).getOrElse(Nil),
    ns = Option(v.getNS).map(_.asScala).getOrElse(Nil),
    bs = Option(v.getBS).map(_.asScala).getOrElse(Nil)
  )
}

case class AttributeValue(
    s: Option[String] = None,
    n: Option[String] = None,
    b: Option[ByteBuffer] = None,
    m: Option[JMap[String, aws.model.AttributeValue]] = None,
    l: Seq[aws.model.AttributeValue],
    ss: Seq[String] = Nil,
    ns: Seq[String] = Nil,
    bs: Seq[ByteBuffer] = Nil) extends aws.model.AttributeValue {

  setS(s.orNull[String])
  setN(n.orNull[String])
  setB(b.orNull[ByteBuffer])
  setM(m.orNull[JMap[String, aws.model.AttributeValue]])
  setL(l.asJavaCollection)
  setSS(ss.asJava)
  setNS(ns.asJava)
  setBS(bs.asJava)
}

