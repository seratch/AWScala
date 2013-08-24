package awscala.s3

import com.amazonaws.services.{ s3 => aws }

object Owner {
  def apply(o: aws.model.Owner) = new Owner(o.getId, o.getDisplayName)
}

case class Owner(id: String, displayName: String) extends aws.model.Owner(id, displayName)

