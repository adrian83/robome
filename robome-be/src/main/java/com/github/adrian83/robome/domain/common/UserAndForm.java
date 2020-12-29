package com.github.adrian83.robome.domain.common;

import com.github.adrian83.robome.domain.user.model.User;
import com.github.adrian83.robome.web.common.Validation;

public class UserAndForm<T extends Validator> {

  private T form;
  private User user;

  public UserAndForm(User user, T form) {
    this.user = user;
    this.form = form;
  }

  public UserAndForm<T> validate() {
    Validation.validate(form);
    return new UserAndForm<T>(user, form);
  }

  public T getForm() {
    return form;
  }

  public User getUser() {
    return user;
  }
}
