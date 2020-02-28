package com.github.adrian83.robome.common.web;

public class ValidationError {

	private String field;
	private String messageCode;
	private String message;

	public ValidationError(String field, String messageCode, String message) {
		super();
		this.field = field;
		this.messageCode = messageCode;
		this.message = message;
	}

	public String getField() {
		return field;
	}

	public String getMessageCode() {
		return messageCode;
	}

	public String getMessage() {
		return message;
	}

}
