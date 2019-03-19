package com.github.adrian83.robome.util.http;

public enum HttpHeader {

	CONTENT_TYPE("Content-Type"),
	AUTHORIZATION("Authorization");
	
	private String text;
	
	HttpHeader(String text) {
		this.text = text;
	}
	
	public String getText() {
		return this.text;
	}
	
	
}
