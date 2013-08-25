package awscala.dynamodb

import scala.collection.JavaConverters._
import com.amazonaws.services.{ dynamodbv2 => aws }

object Projection {
  def apply(p: aws.model.Projection): Projection = new Projection(
    nonKeyAttributes = p.getNonKeyAttributes.asScala,
    projectionType = aws.model.ProjectionType.fromValue(p.getProjectionType)
  )
}

case class Projection(nonKeyAttributes: Seq[String], projectionType: ProjectionType) extends aws.model.Projection {
  setNonKeyAttributes(nonKeyAttributes.asJava)
  setProjectionType(projectionType)
}

