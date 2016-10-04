package awscala

import awscala.dynamodbv2._
import com.amazonaws.services.{ dynamodbv2 => aws }
import org.scalatest._
import org.slf4j._
import scala.util.Try

class DynamoDBV2Spec extends FlatSpec with Matchers {

  behavior of "DynamoDB"

  val log = LoggerFactory.getLogger(this.getClass)

  it should "provide cool APIs for Hash PK tables" in {
    implicit val dynamoDB = DynamoDB.local()

    val tableName = s"Companies_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Id" -> AttributeType.String
    )
    log.info(s"Created Table: ${createdTableMeta}")

    println(s"Waiting for DynamoDB table activation...")
    var isTableActivated = false
    while (!isTableActivated) {
      dynamoDB.describe(createdTableMeta.table).map { meta =>
        isTableActivated = meta.status == aws.model.TableStatus.ACTIVE
      }
      Thread.sleep(1000L)
      print(".")
    }
    println("")
    println(s"Created DynamoDB table has been activated.")

    val companies: Table = dynamoDB.table(tableName).get

    companies.put("Amazon", "url" -> "http://www.amazon.com/")
    companies.put("Google", "url" -> "http://www.google.com/")
    companies.put("Microsoft")

    // get by primary key
    val google: Option[Item] = companies.get("Google")
    google.get.attributes.find(_.name == "url").get.value.s.get should equal("http://www.google.com/")

    val nonExistant: Option[Item] = companies.get("I Don't Exist")
    nonExistant.isDefined should not be true

    // batch get
    val batchedCompanies: Seq[Item] = companies.batchGet(List(("Id", "Google"), ("Id", "Microsoft")))
    batchedCompanies.size should equal(2)
    batchedCompanies.map(item => item.attributes.find(_.name == "Id").get.value.s.get.equals("Google")
      || item.attributes.find(_.name == "Id").get.value.s.get.equals("Microsoft")) should equal(Seq(true, true))

    val batchedNonExistant: Seq[Item] = companies.batchGet(List(("Id", "I Don't Exist"), ("Id", "Neither Do I")))
    batchedNonExistant.size should equal(0)

    // scan
    val foundCompanies: Seq[Item] = companies.scan(Seq("url" -> cond.isNotNull))
    foundCompanies.size should equal(2)

    val scanNonExistant: Seq[Item] = companies.scan(Seq("url" -> cond.eq("I Don't Exist")))
    scanNonExistant.size should equal(0)

    // putAttributes
    companies.putAttributes("Microsoft", Seq("url" -> "http://www.microsoft.com"))
    companies.get("Microsoft").get.attributes.find(_.name == "url").get.value.s.get should equal("http://www.microsoft.com")

    companies.destroy()
  }

  it should "provide cool APIs for Hash/Range PK tables" in {
    implicit val dynamoDB = DynamoDB.local()

    val tableName = s"Members_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Id" -> AttributeType.Number,
      rangePK = "Country" -> AttributeType.String,
      otherAttributes = Seq("Company" -> AttributeType.String),
      indexes = Seq(
        LocalSecondaryIndex(
          name = "CompanyIndex",
          keySchema = Seq(KeySchema("Id", KeyType.Hash), KeySchema("Company", KeyType.Range)),
          projection = Projection(ProjectionType.Include, Seq("Company"))
        )
      )
    )
    log.info(s"Created Table: ${createdTableMeta}")

    println(s"Waiting for DynamoDB table activation...")
    var isTableActivated = false
    while (!isTableActivated) {
      dynamoDB.describe(createdTableMeta.table).map { meta =>
        isTableActivated = meta.status == aws.model.TableStatus.ACTIVE
      }
      Thread.sleep(1000L)
      print(".")
    }
    println("")
    println(s"Created DynamoDB table has been activated.")

    val members: Table = dynamoDB.table(tableName).get

    members.put(1, "Japan", "Name" -> "Alice", "Age" -> 23, "Company" -> "Google")
    members.put(2, "U.S.", "Name" -> "Bob", "Age" -> 36, "Company" -> "Google")
    members.put(3, "Japan", "Name" -> "Chris", "Age" -> 29, "Company" -> "Amazon")

    val nonExistant: Option[Item] = members.get(4, "U.K.")
    nonExistant.isDefined should not be true

    val googlers: Seq[Item] = members.scan(Seq("Company" -> cond.eq("Google")))
    googlers.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should equal(Seq("Bob", "Alice"))

    val scanNonExistant: Seq[Item] = members.scan(Seq("Company" -> cond.eq("I Don't Exist")))
    scanNonExistant.size should equal(0)

    // putAttributes
    members.putAttributes(3, "Japan", Seq("Company" -> "Microsoft"))
    members.get(3, "Japan").get.attributes.find(_.name == "Company").get.value.s.get should equal("Microsoft")

    val exp = DynamoDBExpectedAttributeValue
    Try(dynamoDB.putConditional(tableName, "Id" -> 3, "Country" -> "Japan",
      "Name" -> "Kris")(Seq("Age" -> exp.lt(29)))) should be a 'failure

    Try(dynamoDB.putConditional(tableName, "Id" -> 3, "Country" -> "Japan",
      "Name" -> "Kris")(Seq("Age" -> exp.lt(30)))) should be a 'success

    members.destroy()
  }

  it should "convert maps to attribute values implicitly" in {
    implicit val dynamoDB = DynamoDB.local()

    val tableName = s"Members_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Id" -> AttributeType.Number,
      rangePK = "Country" -> AttributeType.String,
      otherAttributes = Seq("Company" -> AttributeType.String),
      indexes = Seq(
        LocalSecondaryIndex(
          name = "CompanyIndex",
          keySchema = Seq(KeySchema("Id", KeyType.Hash), KeySchema("Company", KeyType.Range)),
          projection = Projection(ProjectionType.Include, Seq("Company"))
        )
      )
    )
    log.info(s"Created Table: ${createdTableMeta}")

    println(s"Waiting for DynamoDB table activation...")
    var isTableActivated = false
    while (!isTableActivated) {
      dynamoDB.describe(createdTableMeta.table).map { meta =>
        isTableActivated = meta.status == aws.model.TableStatus.ACTIVE
      }
      Thread.sleep(1000L)
      print(".")
    }
    println("")
    println(s"Created DynamoDB table has been activated.")

    val members: Table = dynamoDB.table(tableName).get

    members.put(1, "Japan", "Name" -> Map("foo" -> Map("bar" -> "brack")), "Age" -> 23, "Company" -> "Google")
    members.get(1, "Japan").get.attributes.find(_.name == "Name").get.value.m.get.get("foo").getM().get("bar").getS() should equal("brack")

    members.put(2, "Micronesia", "Name" -> Map("aliases" -> List("foo", "bar", "other")), "Age" -> 26, "Company" -> "Spotify")
    members.get(2, "Micronesia").get.attributes.find(_.name == "Name").get.value.m.get.get("aliases").getSS() should contain allOf ("foo", "bar", "other")
  }

  it should "convert list of maps to attribute values implicitly" in {
    implicit val dynamoDB = DynamoDB.local()

    val tableName = s"Members_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Id" -> AttributeType.Number,
      rangePK = "Country" -> AttributeType.String,
      otherAttributes = Seq("Company" -> AttributeType.String),
      indexes = Seq(
        LocalSecondaryIndex(
          name = "CompanyIndex",
          keySchema = Seq(KeySchema("Id", KeyType.Hash), KeySchema("Company", KeyType.Range)),
          projection = Projection(ProjectionType.Include, Seq("Company"))
        )
      )
    )
    log.info(s"Created Table: ${createdTableMeta}")

    println(s"Waiting for DynamoDB table activation...")
    var isTableActivated = false
    while (!isTableActivated) {
      dynamoDB.describe(createdTableMeta.table).map { meta =>
        isTableActivated = meta.status == aws.model.TableStatus.ACTIVE
      }
      Thread.sleep(1000L)
      print(".")
    }
    println("")
    println(s"Created DynamoDB table has been activated.")

    val members: Table = dynamoDB.table(tableName).get

    members.put(1, "Japan", "Name" -> List(Map("bar" -> "brack")), "Age" -> 23, "Company" -> "Google")
    members.get(1, "Japan").get.attributes.find(_.name == "Name").get.value.m.get.get(0).getM()
      .get("bar").getS() should equal("brack")
  }

  it should "provide cool APIs to use global secondary index" in {
    implicit val dynamoDB = DynamoDB.local()

    val tableName = s"Users_${System.currentTimeMillis}"
    val globalSecondaryIndex = GlobalSecondaryIndex(
      name = "SexIndex",
      keySchema = Seq(KeySchema("Sex", KeyType.Hash), KeySchema("Age", KeyType.Range)),
      projection = Projection(ProjectionType.All),
      provisionedThroughput = ProvisionedThroughput(readCapacityUnits = 10, writeCapacityUnits = 10)
    )
    val table = Table(
      name = tableName,
      hashPK = "Id",
      attributes = Seq(
        AttributeDefinition("Id", AttributeType.Number),
        AttributeDefinition("Sex", AttributeType.String),
        AttributeDefinition("Age", AttributeType.Number)
      ),
      globalSecondaryIndexes = Seq(globalSecondaryIndex)
    )
    val createdTableMeta: TableMeta = dynamoDB.createTable(table)
    log.info(s"Created Table: ${createdTableMeta}")

    println(s"Waiting for DynamoDB table activation...")
    var isTableActivated = false
    while (!isTableActivated) {
      dynamoDB.describe(createdTableMeta.table).map { meta =>
        isTableActivated = meta.status == aws.model.TableStatus.ACTIVE
      }
      Thread.sleep(1000L)
      print(".")
    }
    println("")
    println(s"Created DynamoDB table has been activated.")

    val users: Table = dynamoDB.table(tableName).get

    users.put(1, "Name" -> "John", "Sex" -> "Male", "Age" -> 12)
    users.put(2, "Name" -> "Bob", "Sex" -> "Male", "Age" -> 14, "Friend" -> true)
    users.put(3, "Name" -> "Chris", "Sex" -> "Female", "Age" -> 9)
    users.put(4, "Name" -> "Michael", "Sex" -> "Male", "Age" -> 65)

    val teenageBoys: Seq[Item] = users.queryWithIndex(
      index = globalSecondaryIndex,
      keyConditions = Seq("Sex" -> cond.eq("Male"), "Age" -> cond.lt(20)),
      limit = 1, // to test that we still return 2 names
      pageStatsCallback = println
    )

    teenageBoys.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should equal(Seq("John", "Bob"))
    teenageBoys.flatMap(_.attributes.find(_.name == "Friend")).map(_.value.bl.get) should equal(Seq(true))
    users.destroy()
  }

  it should "support paging for table scans" in {
    implicit val dynamoDB = DynamoDB.local()

    val tableName = s"Cities_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Id" -> AttributeType.Number,
      rangePK = "Country" -> AttributeType.String,
      otherAttributes = Seq(),
      indexes = Seq()
    )
    log.info(s"Created Table: ${createdTableMeta}")

    println(s"Waiting for DynamoDB table activation...")
    var isTableActivated = false
    while (!isTableActivated) {
      dynamoDB.describe(createdTableMeta.table).map { meta =>
        isTableActivated = meta.status == aws.model.TableStatus.ACTIVE
      }
      Thread.sleep(1000L)
      print(".")
    }
    println("")
    println(s"Created DynamoDB table has been activated.")

    val cities: Table = dynamoDB.table(tableName).get

    cities.put(1, "China", "Name" -> "Beijing", "Population" -> 21516000)
    cities.put(2, "Egypt", "Name" -> "Cairo", "Population" -> 9278441)
    cities.put(3, "India", "Name" -> "Delhi", "Population" -> 16787941)
    cities.put(4, "China", "Name" -> "Guangzhou", "Population" -> 9865702)
    cities.put(5, "Turkey", "Name" -> "Istanbul", "Population" -> 14657000)
    cities.put(6, "Indonesia", "Name" -> "Jakarta", "Population" -> 10075310)
    cities.put(7, "Pakistan", "Name" -> "Karachi", "Population" -> 21000000)
    cities.put(8, "Democratic Republic of the Congo", "Name" -> "Kinshasa", "Population" -> 9735000)
    cities.put(9, "Nigeria", "Name" -> "Lagos", "Population" -> 16060303)
    cities.put(10, "Peru", "Name" -> "Lima", "Population" -> 8693387)
    cities.put(11, "United Kingdom", "Name" -> "London", "Population" -> 8538689)
    cities.put(12, "Mexico", "Name" -> "Mexico City", "Population" -> 8874724)
    cities.put(13, "Russia", "Name" -> "Moscow", "Population" -> 12197596)
    cities.put(14, "India", "Name" -> "Mumbai", "Population" -> 12478447)
    cities.put(15, "United States", "Name" -> "New York", "Population" -> 8491079)
    cities.put(16, "South Korea", "Name" -> "Seoul", "Population" -> 10048593)
    cities.put(17, "China", "Name" -> "Shanghai", "Population" -> 24256800)
    cities.put(18, "China", "Name" -> "Shenzhen", "Population" -> 10780000)
    cities.put(19, "Brazil", "Name" -> "São Paulo", "Population" -> 21292893)
    cities.put(20, "Japan", "Name" -> "Tokyo", "Population" -> 13297629)

    // set up a closure with page stats, and a way to reset and add to that
    var pages = 0
    var scanned = 0
    var found = 0
    def resetCounts = {
      pages = 0
      scanned = 0
      found = 0
    }
    def addPageCounts(page: PageStats) = {
      pages += 1
      scanned += page.scanned
      found += page.items
    }

    // a limit of 2, with 20 items, will divide into 10 pages
    // (and need 11 page fetches since DynamoDB needs to fetch an additional page to find out there was no more data)
    // a filter of population > 20M should return 4/20 cities, so at least 7 pages will have no matching results
    val huge1: Seq[Item] = cities.scan(Seq("Population" -> cond.gt(20000000)), limit = 2, pageStatsCallback = addPageCounts)
    huge1.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should contain only ("Karachi", "Beijing", "São Paulo", "Shanghai")
    pages should be(11)
    scanned should be(20)
    found should be(4)
    resetCounts

    // a limit of 3, with 20 items, will divide into 7 pages
    // (and need 7 page fetches as the last page is partial so DynamoDB can tell it's done)
    // a filter of population > 20M should return 4/20 cities, so at least 3 pages will have no matching results
    val huge2: Seq[Item] = cities.scan(Seq("Population" -> cond.gt(20000000)), limit = 3, pageStatsCallback = addPageCounts)
    huge2.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should contain only ("Beijing", "Karachi", "Shanghai", "São Paulo")
    pages should be(7)
    scanned should be(20)
    found should be(4)
    resetCounts

    // a filter of population > 2 should return 20/20 cities, and a limit of 101 gives all results on a single page
    val all1: Seq[Item] = cities.scan(Seq("Population" -> cond.gt(2)), limit = 101, pageStatsCallback = addPageCounts)
    all1.size should be(20)
    pages should be(1)
    scanned should be(20)
    found should be(20)
    resetCounts

    // but if you only take a few items from the sequence, it shouldn't fetch more pages than needed
    val all1b: Seq[Item] = cities.scan(Seq("Population" -> cond.gt(2)), limit = 3, pageStatsCallback = addPageCounts)
    val List(first, second) = all1b.take(2).flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)).toList
    pages should be(1) // it should only fetch a single page
    scanned should be(3) // but it would scan the entire page,
    found should be(3) // and find every match on the page, even though we just asked for one (from that page)
    resetCounts

    // a filter of population > 2 should return 20/20 cities, and a limit of 11 gives two pages with results on both
    val all2: Seq[Item] = cities.scan(Seq("Population" -> cond.gt(2)), limit = 11, pageStatsCallback = addPageCounts)
    all2.size should be(20)
    pages should be(2)
    scanned should be(20)
    found should be(20)
    resetCounts

    // the same query should work fine without a callback
    val all3: Seq[Item] = cities.scan(Seq("Population" -> cond.gt(2)), limit = 11, pageStatsCallback = addPageCounts)
    all3.size should be(20)

    cities.destroy()
  }

  it should "support paging for table queries" in {
    implicit val dynamoDB = DynamoDB.local()

    val tableName = s"Cities_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Country" -> AttributeType.String,
      rangePK = "Population" -> AttributeType.Number,
      otherAttributes = Seq(),
      indexes = Seq()
    )
    log.info(s"Created Table: ${createdTableMeta}")

    println(s"Waiting for DynamoDB table activation...")
    var isTableActivated = false
    while (!isTableActivated) {
      dynamoDB.describe(createdTableMeta.table).map { meta =>
        isTableActivated = meta.status == aws.model.TableStatus.ACTIVE
      }
      Thread.sleep(1000L)
      print(".")
    }
    println("")
    println(s"Created DynamoDB table has been activated.")

    val cities: Table = dynamoDB.table(tableName).get

    cities.put("China", 21516000, "Name" -> "Beijing")
    cities.put("Egypt", 9278441, "Name" -> "Cairo")
    cities.put("India", 16787941, "Name" -> "Delhi")
    cities.put("China", 9865702, "Name" -> "Guangzhou")
    cities.put("Turkey", 14657000, "Name" -> "Istanbul")
    cities.put("Indonesia", 10075310, "Name" -> "Jakarta")
    cities.put("Pakistan", 21000000, "Name" -> "Karachi")
    cities.put("Democratic Republic of the Congo", 9735000, "Name" -> "Kinshasa")
    cities.put("Nigeria", 16060303, "Name" -> "Lagos")
    cities.put("Peru", 8693387, "Name" -> "Lima")
    cities.put("United Kingdom", 8538689, "Name" -> "London")
    cities.put("Mexico", 8874724, "Name" -> "Mexico City")
    cities.put("Russia", 12197596, "Name" -> "Moscow")
    cities.put("India", 12478447, "Name" -> "Mumbai")
    cities.put("United States", 8491079, "Name" -> "New York")
    cities.put("South Korea", 10048593, "Name" -> "Seoul")
    cities.put("China", 24256800, "Name" -> "Shanghai")
    cities.put("China", 10780000, "Name" -> "Shenzhen")
    cities.put("Brazil", 21292893, "Name" -> "São Paulo")
    cities.put("Japan", 13297629, "Name" -> "Tokyo")

    // set up a closure with page stats, and a way to reset and add to that
    var pages = 0
    var scanned = 0
    var found = 0
    def resetCounts = {
      pages = 0
      scanned = 0
      found = 0
    }
    def addPageCounts(page: PageStats) = {
      pages += 1
      scanned += page.scanned
      found += page.items
    }

    // a limit of 1, with 2 matching Chinese cities, will divide into 2 pages
    // (and need 3 page fetches since DynamoDB needs to fetch an additional page to find out there was no more data)
    // a filter of population > 20M should return 2 matching Chinese cities
    val hugeChinese1: Seq[Item] = cities.query(Seq("Country" -> cond.eq("China"), "Population" -> cond.gt(20000000)), limit = 1, pageStatsCallback = addPageCounts)
    hugeChinese1.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should contain only ("Beijing", "Shanghai")
    pages should be(3)
    scanned should be(2)
    found should be(2)
    resetCounts

    // a limit of 2, with 3 matching Chinese cities, will divide into 2 pages
    // (and need 2 page fetches as the last page is partial so DynamoDB can tell it's done)
    // a filter of population > 10M should return 3 matching Chinese cities
    val hugeChinese2: Seq[Item] = cities.query(Seq("Country" -> cond.eq("China"), "Population" -> cond.gt(10000000)), limit = 2, pageStatsCallback = addPageCounts)
    hugeChinese2.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should contain only ("Shanghai", "Shenzhen", "Beijing")
    pages should be(2)
    scanned should be(3)
    found should be(3)
    resetCounts

    // but if you only take a few items from the sequence, it shouldn't fetch more pages than needed
    val hugeChinese2b: Seq[Item] = cities.query(Seq("Country" -> cond.eq("China"), "Population" -> cond.gt(10000000)), limit = 2, pageStatsCallback = addPageCounts)
    hugeChinese2b.take(1).flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should contain oneOf ("Shanghai", "Shenzhen", "Beijing")
    pages should be(1) // it should only fetch a single page
    scanned should be(2) // but it would scan the entire page,
    found should be(2) // and find every match on the page, even though we just asked for one (from that page)
    resetCounts

    // a filter of population > 2 should return 4 matching Chinese cities, and a limit of 11 gives all results on a single page
    val allChinese1: Seq[Item] = cities.query(Seq("Country" -> cond.eq("China"), "Population" -> cond.gt(2)), limit = 11, pageStatsCallback = addPageCounts)
    allChinese1.size should be(4)
    pages should be(1)
    scanned should be(4)
    found should be(4)
    resetCounts

    // the same query should work fine without a callback
    val allChinese2: Seq[Item] = cities.query(Seq("Country" -> cond.eq("China"), "Population" -> cond.gt(2)), limit = 11)
    allChinese2.size should be(4)

    cities.destroy()
  }
}
