package awscala.simpledb

import com.amazonaws.services.{ simpledb => aws }

object Attribute {

  def apply(item: Item, a: aws.model.Attribute): Attribute = new Attribute(
    item = item,
    name = a.getName,
    value = a.getValue,
    alternateNameEncoding = a.getAlternateNameEncoding,
    alternateValueEncoding = a.getAlternateValueEncoding
  )
}

case class Attribute(
  item: Item, name: String, value: String, alternateNameEncoding: String, alternateValueEncoding: String)
    extends aws.model.Attribute {

  setAlternateNameEncoding(alternateNameEncoding)
  setAlternateValueEncoding(alternateValueEncoding)
  setName(name)
  setValue(value)

  def update(newValue: String)(implicit simpleDB: SimpleDB): Attribute = {
    simpleDB.replaceAttributesIfExists(item, name -> newValue)
    simpleDB.attributes(item).find(_.name == name).get
  }

  def destroy()(implicit simpleDB: SimpleDB): Unit = {
    simpleDB.deleteAttributes(Seq(this))
  }

}
