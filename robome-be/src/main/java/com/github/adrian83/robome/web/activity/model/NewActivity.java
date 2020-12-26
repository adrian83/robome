package com.github.adrian83.robome.web.activity.model;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString
@EqualsAndHashCode
public class NewActivity {

  private static final String NAME_LABEL = "name";

  private String name;

  @JsonCreator
  public NewActivity(@JsonProperty(NAME_LABEL) String name) {
    super();
    this.name = name;
  }
}
