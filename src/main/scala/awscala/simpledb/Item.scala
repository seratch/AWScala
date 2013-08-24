package awscala.simpledb

import scala.collection.JavaConverters._
import com.amazonaws.services.{ simpledb => aws }

object Item {

  def apply(domain: Domain, name: String) = new Item(
    domain = domain, name = name
  )

  def apply(domain: Domain, i: aws.model.Item): Item = {
    val item = new Item(
      domain = domain,
      name = i.getName,
      alternateNameEncoding = Option(i.getAlternateNameEncoding),
      attributes = Nil
    )
    item.copy(attributes = i.getAttributes.asScala.map(a => Attribute(item, a)).toSeq)
  }
}

case class Item(domain: Domain, name: String, alternateNameEncoding: Option[String] = None, attributes: Seq[Attribute] = Nil)
    extends aws.model.Item {

  setAlternateNameEncoding(alternateNameEncoding.orNull[String])
  setAttributes(attributes.map(_.asInstanceOf[aws.model.Attribute]).asJavaCollection)
  setName(name)

  def replaceAttributesIfExists(attributes: (String, String)*)(implicit simpleDB: SimpleDB): Unit = {
    simpleDB.replaceAttributesIfExists(this, attributes: _*)
  }

  def putAttributes(attributes: (String, String)*)(implicit simpleDB: SimpleDB): Unit = {
    simpleDB.putAttributes(this, attributes: _*)
  }

  def deleteAttributes(attributes: Seq[Attribute])(implicit simpleDB: SimpleDB): Unit = {
    simpleDB.deleteAttributes(attributes)
  }
}

