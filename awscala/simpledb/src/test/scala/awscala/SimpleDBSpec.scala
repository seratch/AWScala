package awscala

import awscala._, simpledb._

import org.slf4j._
import org.scalatest._

class SimpleDBSpec extends FlatSpec with Matchers {

  behavior of "SimpleDB"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "provide cool APIs" in {
    implicit val simpleDB = SimpleDB.at(Region.Tokyo)

    val domainName = s"awscala_unittest_${System.currentTimeMillis}"
    val domain: Domain = simpleDB.createDomain(domainName)
    log.info(s"Created Domain: ${domain}")

    domain.put("User-001", "name" -> "Alice", "age" -> "23", "country" -> "America")
    domain.put("User-002", "name" -> "Bob", "age" -> "34", "country" -> "America")
    domain.put("User-003", "name" -> "Chris", "age" -> "27", "country" -> "Japan")

    val items: Seq[Item] = domain.select(s"select * from ${domainName} where country = 'America'")
    log.info(s"Found Items: ${items}")
    items.size should equal(2)

    items.foreach(_.destroy())
    simpleDB.domains.foreach(_.destroy())
  }

}
