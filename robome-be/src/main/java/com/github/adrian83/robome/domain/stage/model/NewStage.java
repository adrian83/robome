package com.github.adrian83.robome.domain.stage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NewStage {

  private static final String NAME_LABEL = "name";

  private String name;

  @JsonCreator
  public NewStage(@JsonProperty(NAME_LABEL) String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
	return "NewStage [name=" + name + "]";
  }

}
