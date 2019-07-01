package com.github.adrian83.robome.util.http;

public enum Header {

	CONTENT_TYPE("Content-Type"),
	AUTHORIZATION("Authorization");
	
	private String text;
	
	Header(String text) {
		this.text = text;
	}
	
	public String getText() {
		return this.text;
	}
	
	
}
