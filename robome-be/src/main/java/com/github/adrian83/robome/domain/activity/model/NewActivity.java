package com.github.adrian83.robome.domain.activity.model;



import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NewActivity {

  private static final String NAME_LABEL = "name";

  private String name;

  @JsonCreator
  public NewActivity(@JsonProperty(NAME_LABEL) String name) {
    super();
    this.name = name;
  }

  public String getName() {
    return name;
  }

}
