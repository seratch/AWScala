package awscala.dynamodbv2

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }
import java.util.{ Map => JMap }

object AttributeValue {

  private def recurseMapValue(valueMap: Map[String, Any]): Map[String, aws.model.AttributeValue] = valueMap.map {
    case (key, xs: Seq[_]) => key -> toJavaValue(xs)
    case (key, vl: Map[_, _]) => key -> {
      val _vl: Map[String, Any] = vl.map { case (k, v) => k.asInstanceOf[String] -> v }
      new aws.model.AttributeValue().withM(recurseMapValue(_vl).asJava)
    }
    case (key: String, vl: Object) => key -> toJavaValue(vl)
  }

  def toJavaValue(v: Any): aws.model.AttributeValue = {
    val value = new aws.model.AttributeValue
    v match {
      case null => null
      case s: String => value.withS(s)
      case bl: Boolean => value.withBOOL(bl)
      case n: java.lang.Number => value.withN(n.toString)
      case b: ByteBuffer => value.withB(b)
      case xs: Seq[_] => xs.headOption match {
        case Some(m: Map[_, _]) => value.withL(xs.map(toJavaValue).asJavaCollection)
        case Some(s: String) => value.withSS(xs.map(_.asInstanceOf[String]).asJava)
        case Some(n: java.lang.Number) => value.withNS(xs.map(_.toString).asJava)
        case Some(s: ByteBuffer) => value.withBS(xs.map(_.asInstanceOf[ByteBuffer]).asJava)
        case Some(v) => value.withSS(xs.map(_.toString).asJava)
        case _ => null
      }
      case m: Map[_, _] => {
        val _m: Map[String, Any] = m.map { case (k, v) => k.asInstanceOf[String] -> v }
        value.withM(recurseMapValue(_m).asJava)
      }
      case _ => null
    }
  }

  def apply(v: aws.model.AttributeValue): AttributeValue = new AttributeValue(
    s = Option(v.getS),
    bl = Option[java.lang.Boolean](v.getBOOL).map(_.booleanValue()),
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
    bl: Option[Boolean] = None,
    n: Option[String] = None,
    b: Option[ByteBuffer] = None,
    m: Option[JMap[String, aws.model.AttributeValue]] = None,
    l: Seq[aws.model.AttributeValue] = Nil,
    ss: Seq[String] = Nil,
    ns: Seq[String] = Nil,
    bs: Seq[ByteBuffer] = Nil
) extends aws.model.AttributeValue {

  setS(s.orNull[String])
  bl.foreach(setBOOL(_))
  setN(n.orNull[String])
  setB(b.orNull[ByteBuffer])
  setM(m.orNull[JMap[String, aws.model.AttributeValue]])
  setL(l.asJavaCollection)
  setSS(ss.asJava)
  setNS(ns.asJava)
  setBS(bs.asJava)
}
