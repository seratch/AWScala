package awscala

import java.time.chrono.Chronology
import java.time.format.DateTimeFormatter

object DateTime {
  import java.time._

  private[this] val UTC = ZoneId.of("UTC")

  def apply(date: java.util.Date): ZonedDateTime =
    ZonedDateTime.ofInstant(date.toInstant, UTC)
  def toDate(dateTime: ZonedDateTime): java.util.Date =
    java.util.Date.from(dateTime.toInstant)

  def now(): ZonedDateTime = ZonedDateTime.now()
  def now(zone: ZoneId): ZonedDateTime = ZonedDateTime.now(zone)
  def now(chronology: Chronology): ZonedDateTime =
    ZonedDateTime.ofInstant(chronology.zonedDateTime(Instant.now()).toInstant, UTC)

  def parse(str: String): ZonedDateTime = ZonedDateTime.parse(str)
  def parse(str: String, formatter: DateTimeFormatter): ZonedDateTime = ZonedDateTime.parse(str, formatter)

}

