package awscala.redshift

object SnapshotType {
  val Manual = SnapshotType("manual")
}

case class SnapshotType(value: String)
