package ab.java.robome.web.common;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;

import ab.java.robome.web.common.response.Cors;
import ab.java.robome.web.common.validation.ValidationError;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.ContentType;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.model.headers.RawHeader;
import akka.http.javadsl.server.AllDirectives;
import akka.http.javadsl.server.Route;

public class AbstractController extends AllDirectives {
	
	
	protected static final String CORS_ORIGIN_KEY = "cors.origin";
	
	protected ObjectMapper objectMapper;
	protected Config config;
	
	protected AbstractController(ObjectMapper objectMapper, Config config) {
		this.objectMapper = objectMapper;
		this.config = config;
	}

	protected Location locationFor(String ... pathElems) {
		return  Location.create(Arrays.stream(pathElems).collect(Collectors.joining("/")));
	}
	
	protected ContentType json() {
		return ContentType.create(ContentTypes.APPLICATION_JSON);
	}
	
	protected RawHeader jwt(String token) {
		return RawHeader.create(ab.java.robome.web.common.HttpHeader.JWT_TOKEN.getText(), token);
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
	
	protected Route onValidationErrors(List<ValidationError> validationErrors){
		 HttpResponse response = HttpResponse.create()
				 .withStatus(StatusCodes.BAD_REQUEST)
				 .withEntity(ContentTypes.APPLICATION_JSON, toBytes(validationErrors))
				 .addHeaders(headers(
						 Cors.allowHeaders("Content-Type"), 
						 Cors.origin("*"), 
						 Cors.methods("POST")
						 ));
		 	
		 return complete(response);
	}
}
