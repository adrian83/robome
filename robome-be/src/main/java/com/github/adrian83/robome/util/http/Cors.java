package com.github.adrian83.robome.util.http;

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
	
	public static RawHeader methods(HttpMethod ... methods) {
		return RawHeader.create("Access-Control-Allow-Methods", join(methods));
	}
	
	public static RawHeader allowHeaders(String ... headers) {
		return RawHeader.create("Access-Control-Allow-Headers", join(headers));
	}
	
	public static RawHeader exposeHeaders(String ... headers) {
		return RawHeader.create("Access-Control-Expose-Headers", join(headers));
	}
	
	private static String join(String ... values) {
		return Arrays.stream(values).collect(Collectors.joining(SEPARATOR));
	}
	
	private static String join(HttpMethod ... methods) {
		return Arrays.stream(methods).map(HttpMethod::name).collect(Collectors.joining(SEPARATOR));
	}
}
