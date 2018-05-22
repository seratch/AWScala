package awscala.redshift

import com.amazonaws.services.{ redshift => aws }

case class IPRange(cidrip: String, status: String) extends aws.model.IPRange {
  setCIDRIP(cidrip)
  setStatus(status)
}

