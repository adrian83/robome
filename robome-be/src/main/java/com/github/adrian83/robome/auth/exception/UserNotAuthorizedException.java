package com.github.adrian83.robome.auth.exception;

public class UserNotAuthorizedException extends RuntimeException {

  private static final long serialVersionUID = 2348940226612428440L;

  public UserNotAuthorizedException(String msg) {
    super(msg);
  }
}
