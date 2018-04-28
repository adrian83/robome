package ab.java.robome.common.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;



public final class TimeUtils {

	public static LocalDateTime utcNow() {
		ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
		return utc.toLocalDateTime();
	}
	
	public static ZoneId utcZoneId() {
		return ZoneOffset.UTC;
	}
	
	public static LocalDateTime toUtcLocalDate(Date date) {
		Instant instant = date.toInstant();
		ZonedDateTime zdt = instant.atZone(utcZoneId());
		return zdt.toLocalDateTime();
	}

	public static Date toDate(LocalDateTime localDateTime) {
		ZonedDateTime zdt = localDateTime.atZone(utcZoneId());
		return Date.from(zdt.toInstant());
	}

	
}
