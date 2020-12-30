package com.github.adrian83.robome.web.common;

import java.util.List;
import java.util.concurrent.CompletionException;

import com.github.adrian83.robome.auth.exception.InvalidSignInDataException;
import com.github.adrian83.robome.auth.exception.TokenNotFoundException;
import com.github.adrian83.robome.auth.exception.UserNotAuthenticatedException;
import com.github.adrian83.robome.auth.exception.UserNotFoundException;
import com.github.adrian83.robome.domain.common.exception.EmailAlreadyInUseException;
import com.google.inject.Inject;

import akka.http.javadsl.model.HttpResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionHandler {

  private static final ValidationError INVALID_EMAIL_OR_PASS_ERROR =
      new ValidationError("email", "login.invalid", "Invalida email or password");
  private static final ValidationError EMAIL_IN_USE_ERROR =
      new ValidationError("email", "register.invalid", "Email already in use");

  private Response responseFactory;

  @Inject
  public ExceptionHandler(Response responseFactory) {
    this.responseFactory = responseFactory;
  }

  public HttpResponse handle(Throwable ex) {
    log.error("Handling exception: {}", ex);

    if (ex instanceof CompletionException) {
      return handle(ex.getCause());
    } else if (ex instanceof ValidationException) {
      return responseFactory.response400(((ValidationException) ex).getErrors());
    } else if (ex instanceof InvalidSignInDataException) {
      return responseFactory.response400(List.of(INVALID_EMAIL_OR_PASS_ERROR));
    } else if (ex instanceof UserNotFoundException) {
      return responseFactory.response401();
    } else if (ex instanceof UserNotAuthenticatedException) {
      return responseFactory.response401();
    } else if (ex instanceof TokenNotFoundException) {
      return responseFactory.response401();
    } else if (ex instanceof EmailAlreadyInUseException) {
      return responseFactory.response400(List.of(EMAIL_IN_USE_ERROR));
    }
    return responseFactory.response500(ex.getMessage());
  }
}
