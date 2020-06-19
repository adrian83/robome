package com.github.adrian83.robome.util.http;

import akka.http.javadsl.model.headers.RawHeader;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static akka.http.javadsl.model.headers.RawHeader.create;

public class Cors {

  private static final String SEPARATOR = ", ";
  private static final String ALLOW_ORGIN_HEADER = "Access-Control-Allow-Origin";
  private static final String ALLOW_METHODS_HEADER = "Access-Control-Allow-Methods";
  private static final String ALLOW_HEADERS_HEADER = "Access-Control-Allow-Headers";
  private static final String ALLOW_EXPOSE_HEADER = "Access-Control-Expose-Headers";

  public static RawHeader origin(String origin) {
    return create(ALLOW_ORGIN_HEADER, origin);
  }

  public static RawHeader methods(HttpMethod... methods) {
    return create(ALLOW_METHODS_HEADER, join(methods));
  }

  public static RawHeader allowHeaders(HttpHeader... headers) {
    return create(ALLOW_HEADERS_HEADER, join(headers));
  }

  public static RawHeader exposeHeaders(HttpHeader... headers) {
    return create(ALLOW_EXPOSE_HEADER, join(headers));
  }

  private static String join(HttpMethod... methods) {
    return stream(methods).map(HttpMethod::getName).collect(joining(SEPARATOR));
  }

  private static String join(HttpHeader... headers) {
    return stream(headers).map(HttpHeader::getText).collect(joining(SEPARATOR));
  }
}
