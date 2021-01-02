package com.github.adrian83.robome.domain.common;

import com.github.adrian83.robome.common.validation.Validation;
import com.github.adrian83.robome.common.validation.Validator;
import com.github.adrian83.robome.domain.user.model.User;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAndForm<T extends Validator> {
  private User user;
  private T form;

  public UserAndForm(User user, T form) {
    this.user = user;
    this.form = form;
  }

  public UserAndForm<T> validate() {
    Validation.validate(form);
    return new UserAndForm<T>(user, form);
  }
}
