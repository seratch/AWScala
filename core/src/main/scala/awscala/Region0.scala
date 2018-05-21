package awscala

object Region0 {

  import com.amazonaws.{ regions => awsregions }

  private[this] var defaultRegion = awsregions.Region.getRegion(awsregions.Regions.DEFAULT_REGION)

  def default(): Region = defaultRegion
  def default(region: Region): Unit = defaultRegion = region

  def apply(name: String): Region = apply(awsregions.Regions.fromName(name))
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

  val CN_NORTHWEST_1 = apply(awsregions.Regions.CN_NORTHWEST_1)
  val Ningxia = CN_NORTHWEST_1

  val EU_CENTRAL_1 = apply(awsregions.Regions.EU_CENTRAL_1)
  val Frankfurt = EU_CENTRAL_1

  val CA_CENTRAL_1 = apply(awsregions.Regions.CA_CENTRAL_1)
  val Canada = CA_CENTRAL_1

  val EU_WEST_1 = apply(awsregions.Regions.EU_WEST_1)
  val Ireland = EU_WEST_1

  val EU_WEST_2 = apply(awsregions.Regions.EU_WEST_2)
  val London = EU_WEST_2

  val EU_WEST_3 = apply(awsregions.Regions.EU_WEST_3)
  val Paris = EU_WEST_3

  val GovCloud = apply(awsregions.Regions.GovCloud)

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
    London,
    Paris,
    GovCloud,
    SaoPaulo,
    NorthernVirginia,
    Ohio,
    NorthernCalifornia,
    Oregon)

}
