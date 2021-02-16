package awscala

import java.util.Date

import awscala.dynamodbv2._
import com.amazonaws.services.dynamodbv2.model.{ ProvisionedThroughputDescription, TableDescription, TableStatus }
import com.amazonaws.services.dynamodbv2.util.TableUtils
import com.amazonaws.services.{ dynamodbv2 => aws }
import org.scalatest._
import org.slf4j._

import scala.util.Try
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class DynamoDBV2Spec extends AnyFlatSpec with Matchers {

  behavior of "DynamoDB"

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  it should "provide cool APIs for Hash PK tables" in {
    implicit val dynamoDB: DynamoDB = DynamoDB.local()

    val tableName = s"Companies_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Id" -> AttributeType.String)
    log.info(s"Created Table: $createdTableMeta")

    println(s"Waiting for DynamoDB table activation...")
    TableUtils.waitUntilActive(dynamoDB, createdTableMeta.name)
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

  case class Member(hashPK: Int, rangePK: String, Name: String, Age: Int, Company: String)
  case class User(Name: String, Age: Int, Company: String)
  it should "allows using case class in put method" in {
    implicit val dynamoDB: DynamoDB = DynamoDB.local()
    val tableName = s"Members_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Id" -> AttributeType.Number,
      rangePK = "Country" -> AttributeType.String,
      otherAttributes = Seq(
        "Company" -> AttributeType.String),
      indexes = Seq(
        LocalSecondaryIndex(
          name = "CompanyIndex",
          keySchema = Seq(KeySchema("Id", KeyType.Hash), KeySchema("Company", KeyType.Range)),
          projection = Projection(ProjectionType.Include, Seq("Company")))))
    log.info(s"Created Table: $createdTableMeta")

    println(s"Waiting for DynamoDB table activation...")
    TableUtils.waitUntilActive(dynamoDB, createdTableMeta.name)
    println("")
    println(s"Created DynamoDB table has been activated.")

    val members: Table = dynamoDB.table(tableName).get

    members.put(1, "Japan", "Name" -> "Alice", "Age" -> 23, "Company" -> "Google")
    val member = Member(2, "PL", "Alex", 29, "DataMass")
    val user = User("Ben", 33, "GCP")
    members.putItem(member)
    members.putItem(3, "PL", user)

    members.get(1, "Japan").get.attributes.find(_.name == "Company").get.value.s.get should equal("Google")
    members.get(2, "PL").get.attributes.find(_.name == "Name").get.value.s.get should equal("Alex")
    members.get(2, "PL").get.attributes.find(_.name == "Company").get.value.s.get should equal("DataMass")
    members.get(3, "PL").get.attributes.find(_.name == "Company").get.value.s.get should equal("GCP")
    members.get(3, "PL").get.attributes.find(_.name == "Name").get.value.s.get should equal("Ben")
    members.destroy()
  }

  case class TestMember(
                     @hashPK
                     id: Int,
                     @rangePK
                     Country: String,
                     Company: String,
                     Name: String,
                     Age: Int)
  it should "allows using case class with annotation in put method" in {
    implicit val dynamoDB: DynamoDB = DynamoDB.local()
    val tableName = s"Members_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Id" -> AttributeType.Number,
      rangePK = "Country" -> AttributeType.String,
      otherAttributes = Seq(
        "Company" -> AttributeType.String),
      indexes = Seq(
        LocalSecondaryIndex(
          name = "CompanyIndex",
          keySchema = Seq(KeySchema("Id", KeyType.Hash), KeySchema("Company", KeyType.Range)),
          projection = Projection(ProjectionType.Include, Seq("Company")))))
    log.info(s"Created Table: $createdTableMeta")

    println(s"Waiting for DynamoDB table activation...")
    TableUtils.waitUntilActive(dynamoDB, createdTableMeta.name)
    println("")
    println(s"Created DynamoDB table has been activated.")

    val members: Table = dynamoDB.table(tableName).get
    val member = TestMember(1, "PL", "DataMass", "Alex", 29)

    members.putItem(member)

    members.get(1, "PL").get.attributes.find(_.name == "Name").get.value.s.get should equal("Alex")
    members.get(1, "PL").get.attributes.find(_.name == "Company").get.value.s.get should equal("DataMass")
    members.get(1, "PL").get.attributes.find(_.name == "Country").get.value.s.get should equal("PL")
    members.destroy()
  }


  it should "provide cool APIs for Hash/Range PK tables" in {
    implicit val dynamoDB: DynamoDB = DynamoDB.local()

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
          projection = Projection(ProjectionType.Include, Seq("Company")))))
    log.info(s"Created Table: $createdTableMeta")

    println(s"Waiting for DynamoDB table activation...")
    TableUtils.waitUntilActive(dynamoDB, createdTableMeta.name)
    println("")
    println(s"Created DynamoDB table has been activated.")

    val members: Table = dynamoDB.table(tableName).get

    members.put(1, "Japan", "Name" -> "Alice", "Age" -> 23, "Company" -> "Google")
    members.put(2, "U.S.", "Name" -> "Bob", "Age" -> 36, "Company" -> "Google")
    members.put(3, "Japan", "Name" -> "Chris", "Age" -> 29, "Company" -> "Amazon")

    // get by primary key
    val exists = members.get(1, "Japan")
    exists.get.attributes.find(_.name == "Company").get.value.s.get should equal("Google")

    val nonExistant: Option[Item] = members.get(4, "U.K.")
    nonExistant.isDefined should not be true

    // batch get
    val batchedMembers: Seq[Item] = members.batchGet(
      List(("Id", 1, "Country", "Japan"), ("Id", 2, "Country", "U.S.")))
    batchedMembers.size should equal(2)
    val ids = batchedMembers.map(_.attributes.find(_.name == "Id").get.value.n.get.toInt).toList
    ids should equal(List(1, 2))
    val batchedNonExistant: Seq[Item] = members.batchGet(
      List(("Id", 42, "Country", "blah"), ("Id", 1337, "Country", "bloo")))
    batchedNonExistant.size should equal(0)

    // scan
    val googlers: Seq[Item] = members.scan(Seq("Company" -> cond.eq("Google")))
    googlers.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should equal(Seq("Bob", "Alice"))

    val scanNonExistant: Seq[Item] = members.scan(Seq("Company" -> cond.eq("I Don't Exist")))
    scanNonExistant.size should equal(0)

    // putAttributes
    members.putAttributes(3, "Japan", Seq("Company" -> "Microsoft"))
    members.get(3, "Japan").get.attributes.find(_.name == "Company").get.value.s.get should equal("Microsoft")

    val exp = DynamoDBExpectedAttributeValue
    Try(dynamoDB.putConditional(tableName, "Id" -> 3, "Country" -> "Japan",
      "Name" -> "Kris")(Seq("Age" -> exp.lt(29)))) should be a Symbol("failure")

    Try(dynamoDB.putConditional(tableName, "Id" -> 3, "Country" -> "Japan",
      "Name" -> "Kris")(Seq("Age" -> exp.lt(30)))) should be a Symbol("success")

    members.destroy()
  }

  it should "convert maps to attribute values implicitly" in {
    implicit val dynamoDB: DynamoDB = DynamoDB.local()

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
          projection = Projection(ProjectionType.Include, Seq("Company")))))
    log.info(s"Created Table: $createdTableMeta")

    println(s"Waiting for DynamoDB table activation...")
    TableUtils.waitUntilActive(dynamoDB, createdTableMeta.name)
    println("")
    println(s"Created DynamoDB table has been activated.")

    val members: Table = dynamoDB.table(tableName).get

    members.put(1, "Japan", "Name" -> Map("foo" -> Map("bar" -> "brack")), "Age" -> 23, "Company" -> "Google")
    members.get(1, "Japan").get.attributes.find(_.name == "Name").get.value.m.get.get("foo").asScala.m.get.get("bar").asScala.s.get should equal("brack")

    members.put(2, "Micronesia", "Name" -> Map("aliases" -> List("foo", "bar", "other")), "Age" -> 26, "Company" -> "Spotify")
    members.get(2, "Micronesia").get.attributes.find(_.name == "Name").get.value.m.get.get("aliases").asScala.ss should contain allOf ("foo", "bar", "other")
  }

  it should "convert list of maps to attribute values implicitly" in {
    implicit val dynamoDB: DynamoDB = DynamoDB.local()

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
          projection = Projection(ProjectionType.Include, Seq("Company")))))
    log.info(s"Created Table: $createdTableMeta")

    println(s"Waiting for DynamoDB table activation...")
    TableUtils.waitUntilActive(dynamoDB, createdTableMeta.name)
    println("")
    println(s"Created DynamoDB table has been activated.")

    val members: Table = dynamoDB.table(tableName).get

    members.put(1, "Japan", "Name" -> List(Map("bar" -> "brack")), "Age" -> 23, "Company" -> "Google")
    members.get(1, "Japan").get.attributes.find(_.name == "Name").get.value.l.head.getM
      .get("bar").getS should equal("brack")
  }

  it should "provide cool APIs to use global secondary index" in {
    implicit val dynamoDB: DynamoDB = DynamoDB.local()

    val tableName = s"Users_${System.currentTimeMillis}"
    val globalSecondaryIndex = GlobalSecondaryIndex(
      name = "SexIndex",
      keySchema = Seq(KeySchema("Sex", KeyType.Hash), KeySchema("Age", KeyType.Range)),
      projection = Projection(ProjectionType.All),
      provisionedThroughput = ProvisionedThroughput(readCapacityUnits = 10, writeCapacityUnits = 10))
    val table = Table(
      name = tableName,
      hashPK = "Id",
      attributes = Seq(
        AttributeDefinition("Id", AttributeType.Number),
        AttributeDefinition("Sex", AttributeType.String),
        AttributeDefinition("Age", AttributeType.Number)),
      globalSecondaryIndexes = Seq(globalSecondaryIndex))
    val createdTableMeta: TableMeta = dynamoDB.createTable(table)
    log.info(s"Created Table: $createdTableMeta")

    println(s"Waiting for DynamoDB table activation...")
    TableUtils.waitUntilActive(dynamoDB, createdTableMeta.name)
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
      pageStatsCallback = println)

    teenageBoys.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should equal(Seq("John", "Bob"))
    teenageBoys.flatMap(_.attributes.find(_.name == "Friend")).map(_.value.bl.get) should equal(Seq(true))
    users.destroy()
  }

  it should "support paging for table scans" in {
    implicit val dynamoDB: DynamoDB = DynamoDB.local()

    val tableName = s"Cities_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Id" -> AttributeType.Number,
      rangePK = "Country" -> AttributeType.String,
      otherAttributes = Seq(),
      indexes = Seq())
    log.info(s"Created Table: $createdTableMeta")

    println(s"Waiting for DynamoDB table activation...")
    TableUtils.waitUntilActive(dynamoDB, createdTableMeta.name)
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
    def resetCounts(): Unit = {
      pages = 0
      scanned = 0
      found = 0
    }
    def addPageCounts(page: PageStats): Unit = {
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
    resetCounts()

    // a limit of 3, with 20 items, will divide into 7 pages
    // (and need 7 page fetches as the last page is partial so DynamoDB can tell it's done)
    // a filter of population > 20M should return 4/20 cities, so at least 3 pages will have no matching results
    val huge2: Seq[Item] = cities.scan(Seq("Population" -> cond.gt(20000000)), limit = 3, pageStatsCallback = addPageCounts)
    huge2.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should contain only ("Beijing", "Karachi", "Shanghai", "São Paulo")
    pages should be(7)
    scanned should be(20)
    found should be(4)
    resetCounts()

    // a filter of population > 2 should return 20/20 cities, and a limit of 101 gives all results on a single page
    val all1: Seq[Item] = cities.scan(Seq("Population" -> cond.gt(2)), limit = 101, pageStatsCallback = addPageCounts)
    all1.size should be(20)
    pages should be(1)
    scanned should be(20)
    found should be(20)
    resetCounts()

    // but if you only take a few items from the sequence, it shouldn't fetch more pages than needed
    val all1b: Seq[Item] = cities.scan(Seq("Population" -> cond.gt(2)), limit = 3, pageStatsCallback = addPageCounts)
    all1b.take(2).flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)).toList.size shouldBe 2
    pages should be(1) // it should only fetch a single page
    scanned should be(3) // but it would scan the entire page,
    found should be(3) // and find every match on the page, even though we just asked for one (from that page)
    resetCounts()

    // a filter of population > 2 should return 20/20 cities, and a limit of 11 gives two pages with results on both
    val all2: Seq[Item] = cities.scan(Seq("Population" -> cond.gt(2)), limit = 11, pageStatsCallback = addPageCounts)
    all2.size should be(20)
    pages should be(2)
    scanned should be(20)
    found should be(20)
    resetCounts()

    // the same query should work fine without a callback
    val all3: Seq[Item] = cities.scan(Seq("Population" -> cond.gt(2)), limit = 11, pageStatsCallback = addPageCounts)
    all3.size should be(20)

    cities.destroy()
  }

  it should "support paging for table queries" in {
    implicit val dynamoDB: DynamoDB = DynamoDB.local()

    val tableName = s"Cities_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Country" -> AttributeType.String,
      rangePK = "Population" -> AttributeType.Number,
      otherAttributes = Seq(),
      indexes = Seq())
    log.info(s"Created Table: $createdTableMeta")

    println(s"Waiting for DynamoDB table activation...")
    TableUtils.waitUntilActive(dynamoDB, createdTableMeta.name)
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
    def resetCounts(): Unit = {
      pages = 0
      scanned = 0
      found = 0
    }
    def addPageCounts(page: PageStats): Unit = {
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
    resetCounts()

    // a limit of 2, with 3 matching Chinese cities, will divide into 2 pages
    // (and need 2 page fetches as the last page is partial so DynamoDB can tell it's done)
    // a filter of population > 10M should return 3 matching Chinese cities
    val hugeChinese2: Seq[Item] = cities.query(Seq("Country" -> cond.eq("China"), "Population" -> cond.gt(10000000)), limit = 2, pageStatsCallback = addPageCounts)
    hugeChinese2.flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should contain only ("Shanghai", "Shenzhen", "Beijing")
    pages should be(2)
    scanned should be(3)
    found should be(3)
    resetCounts()

    // but if you only take a few items from the sequence, it shouldn't fetch more pages than needed
    val hugeChinese2b: Seq[Item] = cities.query(Seq("Country" -> cond.eq("China"), "Population" -> cond.gt(10000000)), limit = 2, pageStatsCallback = addPageCounts)
    hugeChinese2b.take(1).flatMap(_.attributes.find(_.name == "Name").map(_.value.s.get)) should contain oneOf ("Shanghai", "Shenzhen", "Beijing")
    pages should be(1) // it should only fetch a single page
    scanned should be(2) // but it would scan the entire page,
    found should be(2) // and find every match on the page, even though we just asked for one (from that page)
    resetCounts()

    // a filter of population > 2 should return 4 matching Chinese cities, and a limit of 11 gives all results on a single page
    val allChinese1: Seq[Item] = cities.query(Seq("Country" -> cond.eq("China"), "Population" -> cond.gt(2)), limit = 11, pageStatsCallback = addPageCounts)
    allChinese1.size should be(4)
    pages should be(1)
    scanned should be(4)
    found should be(4)
    resetCounts()

    // the same query should work fine without a callback
    val allChinese2: Seq[Item] = cities.query(Seq("Country" -> cond.eq("China"), "Population" -> cond.gt(2)), limit = 11)
    allChinese2.size should be(4)

    cities.destroy()
  }

  it should "support count operations for table queries and scans" in {
    implicit val dynamoDB: DynamoDB = DynamoDB.local()

    val tableName = s"Cities_${System.currentTimeMillis}"
    val createdTableMeta: TableMeta = dynamoDB.createTable(
      name = tableName,
      hashPK = "Country" -> AttributeType.String,
      rangePK = "Population" -> AttributeType.Number,
      otherAttributes = Seq(),
      indexes = Seq())
    log.info(s"Created Table: $createdTableMeta")

    println(s"Waiting for DynamoDB table activation...")
    TableUtils.waitUntilActive(dynamoDB, createdTableMeta.name)
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

    val queryNbrChineseCities = cities.query(Seq("Country" -> cond.eq("China")), aws.model.Select.COUNT)
    queryNbrChineseCities.map(item => item.attributes.find(_.name == "Count").get.value.n.get.toInt).head should be(4)

    val queryNbrIndianCities = cities.query(Seq("Country" -> cond.eq("India")), aws.model.Select.COUNT)
    queryNbrIndianCities.map(item => item.attributes.find(_.name == "Count").get.value.n.get.toInt).head should be(2)

    val queryZeroCounts = cities.query(Seq("Country" -> cond.eq("Italy")), aws.model.Select.COUNT)
    queryZeroCounts.map(item => item.attributes.find(_.name == "Count").get.value.n.get.toInt).head should be(0)

    val scanAllCities = cities.scan(Seq("Population" -> cond.gt(2)), aws.model.Select.COUNT)
    scanAllCities.map(item => item.attributes.find(_.name == "Count").get.value.n.get.toInt).head should be(20)

    val scanNbrSmallCities = cities.scan(Seq("Population" -> cond.lt(10000000)), aws.model.Select.COUNT)
    scanNbrSmallCities.map(item => item.attributes.find(_.name == "Count").get.value.n.get.toInt).head should be(7)

    val scanZeroCounts = cities.scan(Seq("Population" -> cond.lt(0)), aws.model.Select.COUNT)
    scanZeroCounts.map(item => item.attributes.find(_.name == "Count").get.value.n.get.toInt).head should be(0)

    cities.destroy()
  }

  it should "describe table without BillingMode (#199)" in {
    TableMeta(new TableDescription()
      .withTableSizeBytes(0L)
      .withItemCount(0L)
      .withProvisionedThroughput(new ProvisionedThroughputDescription().withNumberOfDecreasesToday(0L).withLastIncreaseDateTime(new Date()).withLastDecreaseDateTime(new Date()).withReadCapacityUnits(0L).withWriteCapacityUnits(0L))
      .withTableStatus(TableStatus.ACTIVE).withBillingModeSummary(null))
      .billingModeSummary shouldBe None
  }
}
