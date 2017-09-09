package ab.java.robome.web.common.response;

import java.util.Arrays;
import java.util.stream.Collectors;

import akka.http.javadsl.model.headers.RawHeader;

public class Cors {
	
	private static final String SEPARATOR = ", ";

	public static RawHeader origin(String origin) {
		return RawHeader.create("Access-Control-Allow-Origin", origin);
	}
	
	public static RawHeader methods(String ... methods) {
		return RawHeader.create("Access-Control-Allow-Methods", join(methods));
	}
	
	public static RawHeader headers(String ... headers) {
		return RawHeader.create("Access-Control-Allow-Headers", join(headers));
	}
	
	private static String join(String ... values) {
		return Arrays.stream(values).collect(Collectors.joining(SEPARATOR));
	}
	
	
}
