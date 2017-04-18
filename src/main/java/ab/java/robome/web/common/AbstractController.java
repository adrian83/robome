package ab.java.robome.web.common;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.AllDirectives;

public class AbstractController extends AllDirectives {
	
	protected ObjectMapper objectMapper;
	
	protected AbstractController(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	protected Location locationFor(String ... pathElems) {
		return  Location.create(Arrays.stream(pathElems).collect(Collectors.joining("/")));
	}
	
	protected String toBytes(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
