package awscala.redshift

import com.amazonaws.services.{ redshift => aws }

object Endpoint {
  def apply(e: aws.model.Endpoint): Endpoint = new Endpoint(
    address = e.getAddress,
    port = e.getPort
  )
}

case class Endpoint(address: String, port: Int) extends aws.model.Endpoint {
  setAddress(address)
  setPort(port)
}

