package awscala.ec2

import awscala._
import scala.collection.JavaConverters._
import com.amazonaws.services.{ ec2 => aws }

case class RunInstancesRequest(imageId: String, min: Int = 1, max: Int = 1) extends aws.model.RunInstancesRequest(imageId, min, max) {
}
