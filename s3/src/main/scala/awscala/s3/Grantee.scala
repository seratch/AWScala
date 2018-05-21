package awscala.s3

import com.amazonaws.services.{ s3 => aws }

object Grantee {
  def apply(g: aws.model.Grantee): Grantee = Grantee(g.getIdentifier, g.getTypeIdentifier)
}
case class Grantee(identifier: String, typeIdentifier: String) extends aws.model.Grantee {
  override def getIdentifier = identifier
  override def getTypeIdentifier = typeIdentifier
  override def setIdentifier(id: String) = throw new UnsupportedOperationException
}

