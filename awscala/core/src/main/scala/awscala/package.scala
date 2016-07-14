package object awscala {
  // Workaround for https://issues.scala-lang.org/browse/SI-7139
  val Region: Region0.type = Region0
  type Region = com.amazonaws.regions.Region
  type DateTime = org.joda.time.DateTime
  type ByteBuffer = java.nio.ByteBuffer
  type File = java.io.File

}

