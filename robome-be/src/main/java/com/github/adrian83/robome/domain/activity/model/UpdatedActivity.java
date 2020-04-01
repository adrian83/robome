package com.github.adrian83.robome.domain.activity.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdatedActivity {

  private static final String NAME_LABEL = "name";

  private String name;

  @JsonCreator
  public UpdatedActivity(@JsonProperty(NAME_LABEL) String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
