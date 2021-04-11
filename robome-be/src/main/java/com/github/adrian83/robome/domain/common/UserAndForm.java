package com.github.adrian83.robome.domain.common;

import com.github.adrian83.robome.auth.model.UserData;
import com.github.adrian83.robome.common.validation.Validation;
import com.github.adrian83.robome.common.validation.Validator;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserAndForm<T extends Validator> {
  private UserData user;
  private T form;

  public UserAndForm(UserData user, T form) {
    this.user = user;
    this.form = form;
  }

  public UserAndForm<T> validate() {
    Validation.validate(form);
    return new UserAndForm<T>(user, form);
  }
}
