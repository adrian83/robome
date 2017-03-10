package ab.java.robome.web.common;

import java.util.Arrays;
import java.util.stream.Collectors;

import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.AllDirectives;

public class AbstractController extends AllDirectives {

	protected Location locationFor(String ... pathElems) {
	
	return  Location.create(Arrays.stream(pathElems).collect(Collectors.joining("/")));
	}
}
