package com.github.adrian83.robome.web.common.http;

public enum HttpMethod {
    POST("POST"), GET("GET"), PUT("PUT"), OPTIONS("OPTIONS"), DELETE("DELETE"), ALL("*");

    private String name;

    HttpMethod(String name) {
	this.name = name;
    }

    public String getName() {
	return name;
    }
}
