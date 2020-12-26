package com.github.adrian83.robome.domain.activity.model.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class UpdatedActivityRequest {

  private static final String NAME_LABEL = "name";

  private String name;

  @JsonCreator
  public UpdatedActivityRequest(@JsonProperty(NAME_LABEL) String name) {
    super();
    this.name = name;
  }

}
