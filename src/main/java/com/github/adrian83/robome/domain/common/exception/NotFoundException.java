package com.github.adrian83.robome.domain.common.exception;

public class NotFoundException extends RuntimeException {

    private static final long serialVersionUID = -6070774930600842135L;

    public NotFoundException(String msg) {
	super(msg);
    }
}
