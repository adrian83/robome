package ab.java.robome.common.time;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;



public final class UTCUtils {

	private static final Object UTC = "UTC";

	public static LocalDateTime utcNow() {
		ZonedDateTime utc = ZonedDateTime.now(ZoneOffset.UTC);
		return utc.toLocalDateTime();
	}
	
	public static ZoneId utcZoneId() {
		String zoneId = ZoneId.SHORT_IDS.get(UTC);
		
		// TODO fixme
		return ZoneId.systemDefault();
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
