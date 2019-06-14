package com.github.adrian83.robome.auth;

public class UserNotFoundException extends RuntimeException {

	private static final long serialVersionUID = 2784811785789487778L;

	public UserNotFoundException(String msg) {
		super(msg);
	}
	
}
