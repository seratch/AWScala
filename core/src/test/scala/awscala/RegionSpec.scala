package awscala


import com.amazonaws.regions.{ Regions => AwsRegions }
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RegionSpec extends AnyFlatSpec with Matchers {

  behavior of "Region0"

  it should "have all of the regions defined" in {
    val regionsFromAws = AwsRegions.values().map(_.getName.toUpperCase).sorted

    val regionsFromAwsScala = Region0.all.toArray.map(_.getName.toUpperCase).sorted

    regionsFromAwsScala.diff(regionsFromAws) shouldBe empty
    regionsFromAws.diff(regionsFromAwsScala) shouldBe empty

    regionsFromAwsScala should contain theSameElementsAs regionsFromAws
  }

}
