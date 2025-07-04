package com.github.adrian83.robome.auth.exception;

public class TokenNotFoundException extends RuntimeException {

  private static final long serialVersionUID = 278481123422487778L;

  public TokenNotFoundException(String msg) {
    super(msg);
  }

  public TokenNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
