package awscala

import org.scalatest.{ FlatSpec, Matchers }

import com.amazonaws.regions.{ Regions => AwsRegions }

class RegionSpec extends FlatSpec with Matchers {

  behavior of "Region0"

  it should "have all of the regions defined" in {
    val regionsFromAws = AwsRegions.values().map(_.getName.toUpperCase).sorted

    val regionsFromAwsScala = Region0.all.toArray.map(_.getName.toUpperCase).sorted

    regionsFromAwsScala should contain theSameElementsAs regionsFromAws

  }

}
