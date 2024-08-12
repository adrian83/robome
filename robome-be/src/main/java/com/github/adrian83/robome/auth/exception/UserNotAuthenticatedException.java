package com.github.adrian83.robome.auth.exception;

public class UserNotAuthenticatedException extends RuntimeException {

    private static final long serialVersionUID = 2348940226612428440L;

    public UserNotAuthenticatedException(String msg) {
        super(msg);
    }
}
