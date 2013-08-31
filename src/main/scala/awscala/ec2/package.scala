package awscala

import scala.collection.JavaConverters._
import com.amazonaws.services.{ ec2 => aws }

package object ec2 {
  def wrapOption[A](value: A): Option[A] = if (null != value) Some(value) else None
  def convOption[A, B](value: A)(conv: A => B): Option[B] = if (null != value) Some(conv(value)) else None

  type InstanceType = com.amazonaws.services.ec2.model.InstanceType

}

