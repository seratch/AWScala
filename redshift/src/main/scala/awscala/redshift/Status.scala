package awscala.redshift

object Status {
  val Creating = Status("creating")
  val Available = Status("available")
  val Failed = Status("failed")
  val Deleted = Status("deleted")
}

case class Status(value: String) {

}
