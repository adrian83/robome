package com.github.adrian83.robome.domain.common;

import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.domain.user.model.User;

public class UserAndForm<T> {

  private T form;
  private User user;
  private Validator<T> validator;

  public UserAndForm(User user, T form, Validator<T> validator) {
    this.user = user;
    this.form = form;
    this.validator = validator;
  }

  public UserAndForm<T> validate() {
    var validated = Validation.validate(form, validator);
    return new UserAndForm<T>(user, validated, validator);
  }

  public T getForm() {
    return form;
  }

  public User getUser() {
    return user;
  }
}
