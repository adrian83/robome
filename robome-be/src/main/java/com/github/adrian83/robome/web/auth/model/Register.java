package com.github.adrian83.robome.web.auth.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Register {

  private static final String EMAIL_LABEL = "email";
  private static final String PASSWORD_1_LABEL = "password";
  private static final String PASSWORD_2_LABEL = "repeatedPassword";

  private String email;
  private String password;
  private String repeatedPassword;

  @JsonCreator
  public Register(
      @JsonProperty(EMAIL_LABEL) String email,
      @JsonProperty(PASSWORD_1_LABEL) String password,
      @JsonProperty(PASSWORD_2_LABEL) String repeatedPassword) {
    super();
    this.email = email;
    this.password = password;
    this.repeatedPassword = repeatedPassword;
  }

  public String getEmail() {
    return email;
  }

  public String getPassword() {
    return password;
  }

  public String getRepeatedPassword() {
    return repeatedPassword;
  }

}
