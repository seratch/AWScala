package awscala

object Region {

  import com.amazonaws.{ regions => awsregions }

  private[this] var defaultRegion = awsregions.Region.getRegion(awsregions.Regions.DEFAULT_REGION)

  def default(): Region = defaultRegion
  def default(region: Region): Unit = defaultRegion = region

  def apply(name: String): Region = apply(awsregions.Regions.fromName(name))
  def apply(name: awsregions.Regions): Region = awsregions.Region.getRegion(name)

  val AP_NORTHEAST_1 = apply(awsregions.Regions.AP_NORTHEAST_1)
  val Tokyo = AP_NORTHEAST_1

  val AP_SOUTHEAST_1 = apply(awsregions.Regions.AP_SOUTHEAST_1)
  val Singapore = AP_SOUTHEAST_1

  val AP_SOUTHEAST_2 = apply(awsregions.Regions.AP_SOUTHEAST_2)
  val Sydney = AP_SOUTHEAST_2

  val EU_WEST_1 = apply(awsregions.Regions.EU_WEST_1)
  val Ireland = EU_WEST_1

  val GovCloud = apply(awsregions.Regions.GovCloud)

  val SA_EAST_1 = apply(awsregions.Regions.SA_EAST_1)
  val SaoPaulo = SA_EAST_1

  val US_EAST_1 = apply(awsregions.Regions.US_EAST_1)
  val NorthernVirginia = US_EAST_1

  val US_WEST_1 = apply(awsregions.Regions.US_WEST_1)
  val NorthernCalifornia = US_WEST_1

  val US_WEST_2 = apply(awsregions.Regions.US_WEST_2)
  val Oregon = US_WEST_2

}
