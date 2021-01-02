package com.github.adrian83.robome.common;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public final class Time {

  private static final ZoneId UTC_ZONE = ZoneOffset.UTC;

  private Time() {}

  public static LocalDateTime utcNow() {
    ZonedDateTime utc = ZonedDateTime.now(UTC_ZONE);
    return utc.toLocalDateTime();
  }

  public static LocalDateTime toUtcLocalDate(Date date) {
    Instant instant = date.toInstant();
    ZonedDateTime zdt = instant.atZone(UTC_ZONE);
    return zdt.toLocalDateTime();
  }

  public static LocalDateTime toUtcLocalDate(Instant instant) {
    ZonedDateTime zdt = instant.atZone(UTC_ZONE);
    return zdt.toLocalDateTime();
  }

  public static Date toDate(LocalDateTime localDateTime) {
    ZonedDateTime zdt = localDateTime.atZone(UTC_ZONE);
    return Date.from(zdt.toInstant());
  }

  public static Instant toInstant(LocalDateTime localDateTime) {
    ZonedDateTime zdt = localDateTime.atZone(UTC_ZONE);
    return zdt.toInstant();
  }
}
