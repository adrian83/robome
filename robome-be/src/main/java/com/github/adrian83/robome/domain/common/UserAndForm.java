package com.github.adrian83.robome.domain.common;

import com.github.adrian83.robome.common.web.Validable;
import com.github.adrian83.robome.common.web.Validation;
import com.github.adrian83.robome.domain.user.User;

public class UserAndForm<T extends Validable> {

  private T form;
  private User user;

  public UserAndForm(User user, T form) {
    this.user = user;
    this.form = form;
  }

  public UserAndForm<T> validate() {
    var validated = Validation.validate(form);
    return new UserAndForm<T>(user, validated);
  }

  public T getForm() {
    return form;
  }

  public User getUser() {
    return user;
  }
}
