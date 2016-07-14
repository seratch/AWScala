package awscala

import org.joda.time.format.DateTimeFormatter

object DateTime {
  import org.joda.time.{ DateTime => Joda, _ }

  def now() = Joda.now()
  def now(zone: DateTimeZone) = Joda.now(zone)
  def now(chronology: Chronology) = Joda.now(chronology)

  def parse(str: String) = Joda.parse(str)
  def parse(str: String, formatter: DateTimeFormatter) = Joda.parse(str, formatter)

}

