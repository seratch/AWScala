package awscala.simpledb

case class Domain(name: String) {

  def metadata()(implicit simpleDB: SimpleDB): DomainMetadata = simpleDB.domainMetadata(this)

  def select(expression: String, consistentRead: Boolean = true)(implicit simpleDB: SimpleDB): Seq[Item] = {
    simpleDB.select(this, expression, consistentRead)
  }

  def replaceIfExists(itemName: String, attributes: (String, String)*)(implicit simpleDB: SimpleDB): Unit = {
    simpleDB.replaceAttributesIfExists(Item(this, itemName), attributes: _*)
  }

  def put(itemName: String, attributes: (String, String)*)(implicit simpleDB: SimpleDB): Unit = {
    simpleDB.putAttributes(Item(this, itemName), attributes: _*)
  }

  def delete(item: Item)(implicit simpleDB: SimpleDB) = deleteItem(item)
  def deleteItem(item: Item)(implicit simpleDB: SimpleDB) = simpleDB.deleteItems(Seq(item))

  def delete(attribute: Attribute)(implicit simpleDB: SimpleDB) = deleteAttribute(attribute)
  def deleteAttribute(attribute: Attribute)(implicit simpleDB: SimpleDB) = simpleDB.deleteAttributes(Seq(attribute))

  def deleteItems(items: Seq[Item])(implicit simpleDB: SimpleDB) = simpleDB.deleteItems(items)
  def deleteAttributes(attributes: Seq[Attribute])(implicit simpleDB: SimpleDB) = simpleDB.deleteAttributes(attributes)

  def destroy()(implicit simpleDB: SimpleDB): Unit = simpleDB.deleteDomain(this)

}

