package com.github.adrian83.robome.common;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import org.slf4j.Logger;

import com.github.adrian83.robome.auth.model.UserData;

public final class Logging {

  private Logging() {}

  public static CompletionStage<UserData> logAction(Logger log, UserData userData, String patter, Object... params) {
	  var email = String.format("[user: {%s}], ", userData.email());
      log.info(email + patter, params);
      return CompletableFuture.completedFuture(userData);
  }
}
