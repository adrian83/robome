package com.github.adrian83.robome.domain.common.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    private static final long serialVersionUID = -6070774930600123135L;

    public EmailAlreadyInUseException(String msg) {
	super(msg);
    }
}
