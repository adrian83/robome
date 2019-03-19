package com.github.adrian83.robome.util.http;

import java.util.List;

import com.google.common.collect.Lists;

import akka.http.javadsl.model.HttpHeader;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;

public class Options {
	
	private static final String DEF_ORIGIN = "*";
	
	private String[] headers = {};
	private String[] methods = {};
	private String origin = DEF_ORIGIN;
	
	public Options() {}
	
	private Options(String[] headers, String[] methods, String origin) {
		super();
		this.headers = headers;
		this.methods = methods;
		this.origin = origin;
	}

	public Options withHeaders(String ...headersNames) {
		return new Options(headersNames, this.methods, this.origin);
	}
	
	public Options withMethods(String ...methodsNames) {
		return new Options(this.headers, methodsNames, this.origin);
	}
	
	public Options withOrigin(String originHost) {
		return new Options(this.headers, this.methods, originHost);
	}
	
	public HttpResponse response() {
		List<HttpHeader> httpHeaders = Lists.newArrayList(
				Cors.origin(origin), 
				Cors.methods(methods), 
				Cors.allowHeaders(headers));
		
		return HttpResponse.create()
				.withStatus(StatusCodes.OK)
				.addHeaders(httpHeaders);
	}

}
