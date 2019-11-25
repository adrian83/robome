package com.github.adrian83.robome.common.web;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.auth.exception.UserNotFoundException;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpResponse;

public class ExceptionHandler {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandler.class);

	private ObjectMapper objectMapper;
	private Response responseFactory;

	@Inject
	public ExceptionHandler(ObjectMapper objectMapper, Response responseFactory) {
		this.objectMapper = objectMapper;
		this.responseFactory = responseFactory;
	}

	public HttpResponse handleException(Throwable ex) {
		
		LOGGER.error("Handling exception: {}", ex);
		
		if(ex instanceof CompletionException) {
			return handleException(ex.getCause());
		} else if (ex instanceof ValidationException) {
			return responseFactory.response400(((ValidationException) ex).getErrors());
		} else if (ex instanceof InvalidSignInDataException) {
			return responseFactory.response400(List.of(new ValidationError("email", "login.invalid", "Invalida email or password")));
		} else if (ex instanceof UserNotFoundException) {
			return responseFactory.response401();
		}
		return responseFactory.response500(ex.getMessage());
	}

	protected String toBytes(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	protected List<HttpHeader> headers(HttpHeader... headers) {
		return Arrays.asList(headers);
	}

}
