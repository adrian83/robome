package com.github.adrian83.robome.common.web;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.adrian83.robome.util.http.Cors;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;

public class Response {

	protected static final String CORS_ORIGIN_KEY = "cors.origin";

	private Config config; 
	private ObjectMapper objectMapper;
	
	
	@Inject
	public Response(Config config, ObjectMapper objectMapper) {
		super();
		this.config = config;
		this.objectMapper = objectMapper;
	}

	public  <T> HttpResponse jsonFromOptional(Optional<T> maybe) {
		return maybe.map(o -> HttpResponse.create().withStatus(StatusCodes.OK)
				.withEntity(ContentTypes.APPLICATION_JSON, toBytes(o)).addHeaders(headers(Cors.origin(corsOrigin()))))
				.orElse(response404());
	}
	
	public HttpResponse jsonFromObject(Object obj) {
		return HttpResponse.create().withStatus(StatusCodes.OK)
				.withEntity(ContentTypes.APPLICATION_JSON, toBytes(obj)).addHeaders(headers(Cors.origin(corsOrigin())));
	}
	
	public HttpResponse response400(List<ValidationError> validationErrors){
		 return HttpResponse.create()
				 .withStatus(StatusCodes.BAD_REQUEST)
				 .withEntity(ContentTypes.APPLICATION_JSON, toBytes(validationErrors))
				 .addHeaders(headers(
						 Cors.allowHeaders("Content-Type"), 
						 Cors.origin("*"), 
						 Cors.methods("POST")
						 ));
	}
	
	public HttpResponse response404() {
		return HttpResponse.create()
				.withStatus(StatusCodes.NOT_FOUND)
				.addHeader(Cors.origin(corsOrigin()));
	}
	
	protected String corsOrigin() {
		return config.getString(CORS_ORIGIN_KEY);
	}
	
	protected List<HttpHeader> headers(HttpHeader ...headers) {
		return Arrays.asList(headers);
	}
	
	protected String toBytes(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	
}
