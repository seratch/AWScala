package awscala.auth.policy

import com.amazonaws.auth.{ policy => aws }

case class Resource(id: String) extends aws.Resource(id)
