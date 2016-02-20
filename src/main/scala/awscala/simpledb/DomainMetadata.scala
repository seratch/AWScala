package awscala.simpledb

import com.amazonaws.services.{ simpledb => aws }

object DomainMetadata {

  def apply(r: aws.model.DomainMetadataResult): DomainMetadata = new DomainMetadata(
    attributeNameCount = r.getAttributeNameCount,
    attributeNamesSizeBytes = r.getAttributeNamesSizeBytes,
    attributeValueCount = r.getAttributeValueCount,
    attributeValuesSizeBytes = r.getAttributeValuesSizeBytes,
    itemCount = r.getItemCount,
    itemNamesSizeBytes = r.getItemNamesSizeBytes,
    timestamp = r.getTimestamp
  )
}
case class DomainMetadata(
  attributeNameCount: Int, attributeNamesSizeBytes: Long,
  attributeValueCount: Int, attributeValuesSizeBytes: Long,
  itemCount: Int, itemNamesSizeBytes: Long,
  timestamp: Int
)

