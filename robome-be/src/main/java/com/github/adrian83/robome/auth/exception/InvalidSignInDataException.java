package com.github.adrian83.robome.auth.exception;

public class InvalidSignInDataException extends RuntimeException {

  private static final long serialVersionUID = 23542353245L;

  public InvalidSignInDataException(String msg) {
    super(msg);
  }

  public InvalidSignInDataException(String message, Throwable cause) {
    super(message, cause);
  }
}
