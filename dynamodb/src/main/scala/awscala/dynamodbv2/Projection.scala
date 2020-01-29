package awscala.dynamodbv2

import scala.jdk.CollectionConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }

object Projection {
  def apply(p: aws.model.Projection): Projection = new Projection(
    nonKeyAttributes = Option(p.getNonKeyAttributes).map(_.asScala).getOrElse(Nil).toSeq,
    projectionType = aws.model.ProjectionType.fromValue(p.getProjectionType))
}

case class Projection(projectionType: ProjectionType, nonKeyAttributes: Seq[String] = Nil) extends aws.model.Projection {
  setProjectionType(projectionType)

  if (projectionType == ProjectionType.Include) {
    setNonKeyAttributes(nonKeyAttributes.asJava)
  } else if (nonKeyAttributes.nonEmpty) {
    throw new IllegalArgumentException("You shouldn't specify `nonKeyAttributes` when ProjectionType is other than INCLUDE.")
  }
}

