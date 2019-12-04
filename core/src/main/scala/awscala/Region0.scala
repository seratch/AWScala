package awscala

object Region0 {

  import com.amazonaws.{ regions => awsregions }

  private[this] var defaultRegion = awsregions.Region.getRegion(awsregions.Regions.DEFAULT_REGION)

  def default(): Region = defaultRegion
  def default(region: Region): Unit = defaultRegion = region

  def apply(name: String): Region = {
    try {
      apply(awsregions.Regions.fromName(name))
    } catch {
      case _: IllegalArgumentException => null
    }
  }

  def apply(name: awsregions.Regions): Region = awsregions.Region.getRegion(name)

  val AP_NORTHEAST_1 = apply(awsregions.Regions.AP_NORTHEAST_1)
  val Tokyo = AP_NORTHEAST_1

  val AP_NORTHEAST_2 = apply(awsregions.Regions.AP_NORTHEAST_2)
  val Seoul = AP_NORTHEAST_2

  val AP_SOUTHEAST_1 = apply(awsregions.Regions.AP_SOUTHEAST_1)
  val Singapore = AP_SOUTHEAST_1

  val AP_SOUTHEAST_2 = apply(awsregions.Regions.AP_SOUTHEAST_2)
  val Sydney = AP_SOUTHEAST_2

  val AP_SOUTH_1 = apply(awsregions.Regions.AP_SOUTH_1)
  val Mumbai = AP_SOUTH_1

  val CN_NORTH_1 = apply(awsregions.Regions.CN_NORTH_1)
  val Beijing = CN_NORTH_1

  // Use string literal to avoid NoSuchFieldError on older AWS SDK versions.
  // Region introduced in AWS SDK for Java 1.11.247:
  // https://github.com/aws/aws-sdk-java/commit/440577a61505f8b0d831106745f8584c007b9cd6
  val CN_NORTHWEST_1 = apply("cn-northwest-1")
  val Ningxia = CN_NORTHWEST_1

  val EU_CENTRAL_1 = apply(awsregions.Regions.EU_CENTRAL_1)
  val Frankfurt = EU_CENTRAL_1

  val CA_CENTRAL_1 = apply(awsregions.Regions.CA_CENTRAL_1)
  val Canada = CA_CENTRAL_1

  val EU_WEST_1 = apply(awsregions.Regions.EU_WEST_1)
  val Ireland = EU_WEST_1

  val EU_WEST_2 = apply(awsregions.Regions.EU_WEST_2)
  val London = EU_WEST_2

  // Use string literal to avoid NoSuchFieldError on older AWS SDK versions.
  // Introduced in AWS SDK for Java 1.11.251:
  // https://github.com/aws/aws-sdk-java/commit/39ebf439b8e4050684cbfca1811c84b3ac5f2468
  val EU_WEST_3 = apply("eu-west-3")
  val Paris = EU_WEST_3

  val EU_NORTH_1 = apply("eu-north-1")
  val Stockholm = EU_NORTH_1

  val GovCloud = apply(awsregions.Regions.GovCloud)
  val USGovWest1 = GovCloud
  val USGovEast1 = apply(awsregions.Regions.US_GOV_EAST_1)

  val SA_EAST_1 = apply(awsregions.Regions.SA_EAST_1)
  val SaoPaulo = SA_EAST_1

  val US_EAST_1 = apply(awsregions.Regions.US_EAST_1)
  val NorthernVirginia = US_EAST_1

  val US_EAST_2 = apply(awsregions.Regions.US_EAST_2)
  val Ohio = US_EAST_2

  val US_WEST_1 = apply(awsregions.Regions.US_WEST_1)
  val NorthernCalifornia = US_WEST_1

  val US_WEST_2 = apply(awsregions.Regions.US_WEST_2)
  val Oregon = US_WEST_2

  lazy val all: Seq[Region] = Seq(
    Tokyo,
    Seoul,
    Singapore,
    Sydney,
    Mumbai,
    Beijing,
    Ningxia,
    Frankfurt,
    Canada,
    Ireland,
    Stockholm,
    London,
    Paris,
    USGovEast1,
    USGovWest1,
    SaoPaulo,
    NorthernVirginia,
    Ohio,
    NorthernCalifornia,
    Oregon)
    .filter(_ != null) // Remove null regions in case of older AWS SDK version.

}
